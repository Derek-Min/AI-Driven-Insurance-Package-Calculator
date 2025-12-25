package insurance_package.service;

import insurance_package.model.PremiumResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    // =====================
    // LIFE EMAIL (SES)
    // =====================
    public String renderLifeQuotationEmail(Map<String, Object> slots,
                                           PremiumResult quote) {

        // -------------------------
        // Build SAFE profile map
        // -------------------------
        Map<String, Object> profile = new java.util.HashMap<>();

        if (slots != null) {
            profile.putAll(slots);
        }

        // 🔒 GUARANTEE all template fields exist
        profile.putIfAbsent("customer_name", "Valued Customer");
        profile.putIfAbsent("age", "N/A");
        profile.putIfAbsent("income", 0);
        profile.putIfAbsent("smoker_status", "N/A");
        profile.putIfAbsent("gender", "Not specified");
        profile.putIfAbsent("occupation", "Not specified");

        Context ctx = new Context();
        ctx.setVariable("profile", profile);

        // quote is used in template
        ctx.setVariable("quote", quote);

        // also used directly
        ctx.setVariable("currency", "MYR");

        return templateEngine.process(
                "life-quotation-email",
                ctx
        );
    }

    // =====================
    // MOTOR EMAIL (SES)
    // =====================
    public String renderMotorQuotationEmail(Map<String, Object> data) {

        Context ctx = new Context();
        ctx.setVariables(data);

        ctx.setVariable("quotationNo", "Q-" + System.currentTimeMillis());
        ctx.setVariable("issueDate", LocalDate.now());
        ctx.setVariable("validUntil", LocalDate.now().plusDays(14));

        return templateEngine.process(
                "motor-quotation-email",
                ctx
        );
    }
}

