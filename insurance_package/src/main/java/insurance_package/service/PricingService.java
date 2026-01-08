package insurance_package.service;

import insurance_package.exception.PricingException;
import insurance_package.exception.ValidationException;
import insurance_package.model.*;
import insurance_package.mongo.repository.CoverageOptionRepository;
import insurance_package.mongo.repository.ProductRepository;
import insurance_package.mongo.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@Profile("mongo")
@RequiredArgsConstructor
public class PricingService {

    private final CoverageOptionRepository coverageOptionRepository;
    private final ProductRepository productRepository;
    private final QuoteRepository quoteRepository;
    private final MotorRuleEngine motorRuleEngine;
    private final LifeRuleEngine lifeRuleEngine;
    private final PdfQuotationService pdfQuotationService;
    private final EmailService emailService;

    public PremiumResult calculatePremium(QuotationRequest req) {
        log.info("Calculating premium for line: {}", req.getLine());
        validate(req);

        Product product = productRepository
                .findFirstByLineAndActive(req.getLine(), true)
                .orElseThrow(() ->
                        new PricingException("No active product for line: " + req.getLine())
                );

        Map<String, Object> rates = product.getBaseRates();

        if (rates == null) {
            rates = new HashMap<>();
            rates.put("base", 400.0);
            rates.put("per_year", 20.0);
            rates.put("currency", "MYR");
        }

        List<CoverageOption> coverageOptions =
                coverageOptionRepository.findByProductId(product.getId());

        PremiumResult result = switch (req.getLine()) {
            case "Motor" -> motorRuleEngine.applyBusinessRules(req, rates, coverageOptions);
            case "Life"  -> lifeRuleEngine.applyBusinessRules(req, rates);
            default      -> throw new PricingException("Unsupported line: " + req.getLine());
        };

        Quote savedQuote = persistQuote(req, result);

        // FIXED: Set quoteId on result if the field exists
        try {
            // Try to set quoteId using reflection if the field exists
            result.getClass().getMethod("setQuoteId", String.class).invoke(result, savedQuote.getQuoteId());
        } catch (Exception e) {
            // If setQuoteId method doesn't exist, just log and continue
            log.debug("PremiumResult does not have setQuoteId method");
        }

        log.info("Premium calculation completed for quote: {}", savedQuote.getQuoteId());
        return result;
    }

    public Quote persistQuote(QuotationRequest req, PremiumResult result) {
        Map<String, Object> slots = req.getSlots();
        Map<String, Object> pb = new HashMap<>();

        pb.put("basePremium", result.getBasePremium());
        pb.put("totalPremium", result.getTotalPremium());
        pb.put("riskScore", result.getRiskScore());

        if (result.getBreakdown() != null) {
            PremiumBreakdown bd = result.getBreakdown();
            pb.put("currency", bd.getCurrency());
            pb.put("sumInsured", bd.getSumInsured());
            pb.put("items", bd.getItems());
            pb.put("summaryExplanation", bd.getSummaryExplanation());
        }

        // Build quote
        Quote quote = Quote.builder()
                .quoteId(generateQuoteId())
                .line(req.getLine())
                .currency(String.valueOf(pb.getOrDefault("currency", "MYR")))
                .customerName(String.valueOf(slots.getOrDefault("customer_name", "Customer")))
                .customerEmail(String.valueOf(slots.getOrDefault("email", "")))
                .requestDetails(slots)
                .premiumBreakdown(pb)
                .totalPremium(result.getTotalPremium())
                .riskScore(result.getRiskScore())
                .status(QuoteStatus.CREATED)
                .createdAt(Instant.now())
                .build();

        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote saved with ID: {}", savedQuote.getQuoteId());

        // Generate PDF
        File pdfFile = null;
        try {
            pdfFile = pdfQuotationService.generateQuotationPdf(savedQuote);
            log.info("PDF generated: {}", pdfFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to generate PDF for quote {}: {}",
                    savedQuote.getQuoteId(), e.getMessage());
            // Continue without PDF
        }

        // Send email if customer email is provided
        String customerEmail = savedQuote.getCustomerEmail();
        if (customerEmail != null && !customerEmail.trim().isEmpty()) {
            try {
                // Send quote email
                emailService.sendQuoteEmail(customerEmail, savedQuote, result);

                // If PDF was generated, send it as attachment
                if (pdfFile != null && pdfFile.exists()) {
                    String subject = String.format("Your %s Insurance Quotation - %s",
                            savedQuote.getLine(),
                            savedQuote.getQuoteId());

                    String body = String.format("""
                        Dear %s,
                        
                        Please find your %s insurance quotation attached.
                        
                        Quote Summary:
                        - Quote ID: %s
                        - Total Premium: %s %s
                        - Risk Score: %s
                        
                        This quotation is valid for 14 days.
                        
                        Best regards,
                        Trust Insurance Team
                        """,
                            savedQuote.getCustomerName(),
                            savedQuote.getLine(),
                            savedQuote.getQuoteId(),
                            savedQuote.getCurrency(),
                            savedQuote.getTotalPremium(),
                            savedQuote.getRiskScore()
                    );

                    emailService.sendQuoteEmailWithAttachment(
                            customerEmail,
                            subject,
                            body,
                            pdfFile
                    );

                    log.info("Email with PDF attachment sent to {}", customerEmail);
                }

            } catch (Exception e) {
                log.error("Failed to send email for quote {}: {}",
                        savedQuote.getQuoteId(), e.getMessage());
                // Don't fail the quote creation if email fails
            }

            // Clean up PDF file after sending
            if (pdfFile != null && pdfFile.exists()) {
                try {
                    boolean deleted = pdfFile.delete();
                    if (deleted) {
                        log.debug("Temporary PDF file deleted: {}", pdfFile.getName());
                    }
                } catch (SecurityException e) {
                    log.warn("Could not delete PDF file: {}", e.getMessage());
                }
            }
        } else {
            log.warn("No email provided for quote {}, email not sent", savedQuote.getQuoteId());
        }

        return savedQuote;
    }

    private void validate(QuotationRequest req) {
        if (req.getLine() == null || req.getLine().trim().isEmpty()) {
            throw new ValidationException("Insurance line is required");
        }
    }

    private String generateQuoteId() {
        String datePart = Instant.now().toString().substring(0, 10).replace("-", "");
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "Q-" + datePart + "-" + randomPart;
    }
}