package insurance_package.service;

import insurance_package.model.PremiumResult;
import insurance_package.model.Quote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Profile("mongo")
@RequiredArgsConstructor
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    // For Life Insurance Email
    public String renderLifeQuotationEmail(
            Map<String, Object> slots,
            PremiumResult quote) {

        Map<String, Object> profile = new HashMap<>();

        if (slots != null) {
            profile.putAll(slots);
        }

        // Ensure all required fields exist
        profile.putIfAbsent("customer_name", "Valued Customer");
        profile.putIfAbsent("age", "N/A");
        profile.putIfAbsent("income", 0);
        profile.putIfAbsent("smoker_status", "N/A");
        profile.putIfAbsent("gender", "Not specified");
        profile.putIfAbsent("occupation", "Not specified");
        profile.putIfAbsent("email", "Not provided");
        profile.putIfAbsent("phone", "Not provided");

        Context ctx = new Context();
        ctx.setVariable("profile", profile);
        ctx.setVariable("quote", quote);
        ctx.setVariable("currency", "MYR");
        ctx.setVariable("today", LocalDate.now());
        ctx.setVariable("formattedDate",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        try {
            return templateEngine.process("life-quotation-email.html", ctx);
        } catch (Exception e) {
            log.error("Failed to render life email template: {}", e.getMessage());
            return generatePlainTextLifeEmail(profile, quote);
        }
    }

    // For Motor Insurance Email
    public String renderMotorQuotationEmail(
            Map<String, Object> slots,
            PremiumResult result,
            Quote quote) {

        Context ctx = new Context();

        // Add all slots data
        if (slots != null) {
            ctx.setVariables(slots);
        }

        // Add quote information
        ctx.setVariable("quote", quote);
        ctx.setVariable("result", result);
        ctx.setVariable("quotationNo", quote.getQuoteId());
        ctx.setVariable("issueDate", LocalDate.now());
        ctx.setVariable("validUntil", LocalDate.now().plusDays(14));
        ctx.setVariable("currency", quote.getCurrency());
        ctx.setVariable("totalPremium", quote.getTotalPremium());
        ctx.setVariable("formattedDate",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        try {
            return templateEngine.process("motor-quotation-email.html", ctx);
        } catch (Exception e) {
            log.error("Failed to render motor email template: {}", e.getMessage());
            return generatePlainTextMotorEmail(slots, quote);
        }
    }

    // Generate email HTML based on quote type
    public String generateQuoteEmail(Quote quote, PremiumResult result) {
        if (quote.getLine() == null) {
            return generateGenericEmail(quote);
        }

        switch (quote.getLine().toLowerCase()) {
            case "life":
                return renderLifeQuotationEmail(quote.getRequestDetails(), result);
            case "motor":
                return renderMotorQuotationEmail(quote.getRequestDetails(), result, quote);
            default:
                return generateGenericEmail(quote);
        }
    }

    private String generatePlainTextLifeEmail(Map<String, Object> profile, PremiumResult quote) {
        return String.format("""
            Dear %s,
            
            Thank you for your Life Insurance quote request.
            
            Customer Information:
            - Age: %s
            - Gender: %s
            - Smoker Status: %s
            - Annual Income: %s
            
            Your quotation has been generated and will be sent shortly.
            
            Best regards,
            Trust Insurance Team
            """,
                profile.getOrDefault("customer_name", "Customer"),
                profile.getOrDefault("age", "N/A"),
                profile.getOrDefault("gender", "Not specified"),
                profile.getOrDefault("smoker_status", "N/A"),
                profile.getOrDefault("income", "N/A")
        );
    }

    private String generatePlainTextMotorEmail(Map<String, Object> slots, Quote quote) {
        return String.format("""
            Dear %s,
            
            Thank you for your Motor Insurance quote request.
            
            Quote ID: %s
            Vehicle: %s %s %s
            Total Premium: %s %s
            
            Your quotation has been generated and will be sent shortly.
            
            Best regards,
            Trust Insurance Team
            """,
                quote.getCustomerName(),
                quote.getQuoteId(),
                slots != null ? slots.getOrDefault("make", "") : "",
                slots != null ? slots.getOrDefault("model", "") : "",
                slots != null ? slots.getOrDefault("year", "") : "",
                quote.getCurrency(),
                quote.getTotalPremium()
        );
    }

    private String generateGenericEmail(Quote quote) {
        return String.format("""
            Dear %s,
            
            Thank you for your insurance quote request.
            
            Quote ID: %s
            Insurance Line: %s
            Total Premium: %s %s
            
            Your quotation has been generated and will be sent shortly.
            
            Best regards,
            Trust Insurance Team
            """,
                quote.getCustomerName(),
                quote.getQuoteId(),
                quote.getLine(),
                quote.getCurrency(),
                quote.getTotalPremium()
        );
    }
}