package insurance_package.service;

import insurance_package.exception.PricingException;
import insurance_package.exception.ValidationException;
import insurance_package.model.*;
import insurance_package.repository.CoverageOptionRepository;
import insurance_package.repository.ProductRepository;
import insurance_package.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.File;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final CoverageOptionRepository coverageOptionRepository;
    private final ProductRepository productRepository;
    private final QuoteRepository quoteRepository;
    private final MotorRuleEngine motorRuleEngine;
    private final LifeRuleEngine lifeRuleEngine;
    private final PdfQuotationService pdfQuotationService;
    private final AwsEmailService awsEmailService;
    private final EmailTemplateService emailTemplateService;


    /**
     * 1️⃣ Calculate premium using rule engines
     * 2️⃣ Persist quote to MongoDB
     * 3️⃣ Generate PDF quotation
     */
    public PremiumResult calculatePremium(QuotationRequest req) {
        validate(req);

        Product product = productRepository
                .findFirstByLineAndActive(req.getLine(), true)
                .orElseThrow(() ->
                        new PricingException("No active product for line: " + req.getLine())
                );

        Map<String, Object> rates = product.getBaseRates();

        if (rates == null) {
            rates = Map.of(
                    "base", 400.0,
                    "per_year", 20.0,
                    "currency", "MYR"
            );
        }

        List<CoverageOption> coverageOptions =
                coverageOptionRepository.findByProductId(product.getId());

        PremiumResult result = switch (req.getLine()) {
            case "Motor" -> motorRuleEngine.applyBusinessRules(req, rates, coverageOptions);
            case "Life"  -> lifeRuleEngine.applyBusinessRules(req, rates);
            default      -> throw new PricingException("Unsupported line: " + req.getLine());
        };

        // ✅ Persist + generate PDF
        persistQuote(req, result);

        System.out.println(
                "FINAL COVERAGES = " + result.getBreakdown().getItems()
                        .stream()
                        .map(i -> i.getLabel())
                        .toList()
        );



        return result;

    }


    /**
     * Validation stays simple and clean
     */
    public void validate(QuotationRequest req) {
        if (req.getLine() == null)
            throw new ValidationException("Line required");

        switch (req.getLine()) {
            case "Motor" -> {
                must(req, "year");
                must(req, "usage");
                must(req, "region");
                must(req, "ncd_percent");
            }
            case "Life" -> {
                must(req, "age");
                must(req, "income");
                must(req, "smoker_status");
            }
            default -> throw new ValidationException("Unsupported line: " + req.getLine());
        }
    }

    /**
     * 🧠 This is where QUOTES belong (MongoDB + PDF)
     */
    public Quote persistQuote(QuotationRequest req, PremiumResult result) {

        Map<String, Object> slots = req.getSlots();

        // -----------------------------
        // 1) Build PREMIUM BREAKDOWN MAP (what PdfQuotationService expects)
        // -----------------------------
        Map<String, Object> pb = new java.util.HashMap<>();

        // Always set these (PDF reads these)
        pb.put("basePremium", result.getBasePremium());
        pb.put("totalPremium", result.getTotalPremium());
        pb.put("riskScore", result.getRiskScore());

        // If motor breakdown exists, include items list + currency + etc.
        if (result.getBreakdown() != null) {
            PremiumBreakdown bd = result.getBreakdown();

            pb.put("currency", bd.getCurrency() != null ? bd.getCurrency() : "MYR");
            pb.put("sumInsured", bd.getSumInsured());
            pb.put("items", bd.getItems() != null ? bd.getItems() : java.util.List.of());
            pb.put("summaryExplanation", bd.getSummaryExplanation());

            // keep it consistent (some templates rely on these)
            pb.put("totalPremium", bd.getTotalPremium());
        } else {
            // Life: no breakdown object in your current design, still give safe defaults
            pb.put("currency", "MYR");
            pb.put("items", java.util.List.of());
        }

        // -----------------------------
        // 2) Build Quote entity
        // -----------------------------
        Quote quote = Quote.builder()
                .quoteId(generateQuoteId())
                .line(req.getLine())
                .currency(String.valueOf(pb.getOrDefault("currency", "MYR")))
                .customerName(String.valueOf(slots.getOrDefault("customer_name", "Customer")))
                .customerEmail(String.valueOf(slots.getOrDefault("email", "")))
                .requestDetails(slots)
                .premiumBreakdown(pb) // ✅ IMPORTANT: store structured map
                .totalPremium(result.getTotalPremium())
                .riskScore(result.getRiskScore())
                .explanation(result.getBreakdown() != null ? result.getBreakdown().getSummaryExplanation() : null)
                .status(QuoteStatus.CREATED)
                .createdAt(Instant.now())
                .build();

        // -----------------------------
        // 3) Persist to MongoDB
        // -----------------------------
        Quote savedQuote = quoteRepository.save(quote);

        // -----------------------------
        // 4) Generate PDF (uses premiumBreakdown map keys)
        // -----------------------------
        File pdfFile = pdfQuotationService.generateQuotationPdf(savedQuote);

        // -----------------------------
        // 5) Send ONLY ONE email (attachment email)
        // -----------------------------
        String subject = "Trust Insurance – " + savedQuote.getLine() + " Insurance Quotation";

        // If you have your thymeleaf email templates, use them here.
        // Example: for motor:
        // String htmlBody = emailTemplateService.renderMotorQuotationEmailFromQuote(savedQuote);
        // For now, keep your existing HTML or your new business template.

        String htmlBody = """
        <html>
          <body style="font-family:Arial,sans-serif;color:#0b1b3a;">
            <h2>Trust Insurance</h2>
            <p>Secure your future</p>
            <p>Dear %s,</p>
            <p>Thank you for choosing Trust Insurance. Your %s Insurance quotation PDF is attached.</p>
            <p><b>Total Premium:</b> %s %s<br/>
               <b>Risk Score:</b> %s</p>
            <p style="color:#60708a;font-size:12px;">
              This quotation is valid for 14 days and subject to underwriting approval.
            </p>
          </body>
        </html>
        """.formatted(
                savedQuote.getCustomerName(),
                savedQuote.getLine(),
                savedQuote.getCurrency(),
                savedQuote.getTotalPremium(),
                savedQuote.getRiskScore()
        );

        awsEmailService.sendQuoteEmailWithAttachment(
                savedQuote.getCustomerEmail(),
                subject,
                htmlBody,
                pdfFile
        );

        return savedQuote;
    }





    private void must(QuotationRequest req, String key) {
        var slots = req.getSlots();
        if (slots == null || slots.get(key) == null)
            throw new ValidationException("Missing required attribute: " + key);
    }


    private String generateQuoteId() {
        return "Q-" + Instant.now().toString().substring(0, 10).replace("-", "")
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
