package insurance_package.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import insurance_package.model.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfQuotationService {

    private final SpringTemplateEngine templateEngine;

    @Value("${quotation.pdf.output-dir:generated-pdfs}")
    private String outputDir;

    // ✅ from application.properties (base64 image string)
    @Value("${company.logo.base64:}")
    private String companyLogoBase64;

    public PdfQuotationService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public File generateQuotationPdf(Quote quote) {

        if (quote == null) {
            throw new IllegalArgumentException("Quote is null");
        }

        try {
            String line = safe(quote.getLine());
            String folder = "Motor".equalsIgnoreCase(line) ? "Motor" : "Life";

            // -------------------------------
            // Prepare Thymeleaf Context
            // -------------------------------
            Context ctx = new Context();

            ctx.setVariable("quotationNo", safe(quote.getQuoteId()));
            ctx.setVariable("issueDate", LocalDate.now());
            ctx.setVariable("validUntil", LocalDate.now().plusDays(14));

            ctx.setVariable("customerName", safe(quote.getCustomerName()));
            ctx.setVariable("currency", safeOrDefault(quote.getCurrency(), "MYR"));
            ctx.setVariable("totalPremium", quote.getTotalPremium() == null ? 0 : quote.getTotalPremium());
            ctx.setVariable("riskScore", quote.getRiskScore());

            // -------------------------------
            // ✅ Logo
            // -------------------------------
            if (companyLogoBase64 != null && !companyLogoBase64.isBlank()) {
                String logo = companyLogoBase64.trim();
                if (!logo.startsWith("data:image")) {
                    logo = "data:image/png;base64," + logo;
                }
                ctx.setVariable("logoBase64", logo);
            } else {
                ctx.setVariable("logoBase64", "");
            }

            // -------------------------------
            // Request details (slots)
            // -------------------------------
            Map<String, Object> req = quote.getRequestDetails();
            if (req == null) req = new HashMap<>();

            // -------------------------------
            // Motor variables (template uses these)
            // -------------------------------
            ctx.setVariable("vehicleMake", safe(req.get("make")));
            ctx.setVariable("vehicleModel", safe(req.get("model")));
            ctx.setVariable("vehicleYear", safe(req.get("year")));
            ctx.setVariable("plateNo", safe(req.get("plate_no")));
            ctx.setVariable("usage", safe(req.get("usage")));
            ctx.setVariable("region", safe(req.get("region")));
            ctx.setVariable("sumInsured", safe(req.get("sum_insured")));

            // -------------------------------
            // ✅ Life variables (template uses profile.age etc)
            // -------------------------------
            if ("Life".equalsIgnoreCase(line)) {
                Map<String, Object> profile = new HashMap<>();
                profile.put("age", req.get("age"));
                profile.put("gender", req.get("gender"));
                profile.put("smoker_status", req.get("smoker_status"));
                profile.put("income", req.get("income"));

                ctx.setVariable("profile", profile);
            }

            // -------------------------------
            // Premium breakdown (COVERAGES) - SAFE
            // -------------------------------
            Map<String, Object> breakdown =
                    quote.getPremiumBreakdown() != null
                            ? quote.getPremiumBreakdown()
                            : new HashMap<>();

            // ✅ basePremium for Life PDF (your template uses it)
            Object basePremium = breakdown.get("basePremium");
            if (basePremium == null) basePremium = breakdown.get("base_premium");
            ctx.setVariable("basePremium", basePremium == null ? 0 : basePremium);

            // ✅ currency from breakdown (if present)
            Object bdCurrency = breakdown.get("currency");
            if (bdCurrency != null && !String.valueOf(bdCurrency).isBlank()) {
                ctx.setVariable("currency", String.valueOf(bdCurrency));
            }

            // ✅ totalPremium from breakdown (if present)
            Object total = breakdown.get("totalPremium");
            if (total != null) {
                ctx.setVariable("totalPremium", total);
            }

            // -------------------------------
            // Motor coverage items list
            // -------------------------------
            Object itemsObj = breakdown.get("items");

            List<Map<String, Object>> coverageItems = new ArrayList<>();

            if (itemsObj instanceof List<?> list) {
                for (Object it : list) {

                    // Case A: Mongo/JSON -> Map
                    if (it instanceof Map<?, ?> m) {
                        String label = m.get("label") == null ? "" : String.valueOf(m.get("label"));
                        Object amount = m.get("amount");

                        Map<String, Object> row = new HashMap<>();
                        row.put("label", label);
                        row.put("amount", amount == null ? 0 : amount);
                        coverageItems.add(row);
                        continue;
                    }

                    // Case B: POJO -> CoverageItem (reflection)
                    try {
                        Object labelObj = it.getClass().getMethod("getLabel").invoke(it);
                        Object amountObj = it.getClass().getMethod("getAmount").invoke(it);

                        Map<String, Object> row = new HashMap<>();
                        row.put("label", labelObj == null ? "" : String.valueOf(labelObj));
                        row.put("amount", amountObj == null ? 0 : amountObj);
                        coverageItems.add(row);
                    } catch (Exception ignore) {
                        // skip unknown type
                    }
                }
            }

            ctx.setVariable("coverageItems", coverageItems);
            System.out.println("PDF coverageItems count = " + coverageItems.size());

            // -------------------------------
            // Select template
            // -------------------------------
            String template = "Motor".equalsIgnoreCase(folder)
                    ? "pdf/motor-quote"
                    : "pdf/life-quote";

            String html = templateEngine.process(template, ctx);

            // -------------------------------
            // Output PDF
            // -------------------------------
            Path dirPath = Path.of(outputDir, folder);
            File dir = dirPath.toFile();

            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new RuntimeException("Failed to create output directory: " + dirPath);
                }
            }

            File pdfFile = dirPath.resolve(quote.getQuoteId() + ".pdf").toFile();

            try (FileOutputStream os = new FileOutputStream(pdfFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();
            }

            return pdfFile;

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    private String safe(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private String safeOrDefault(Object o, String def) {
        String s = safe(o);
        return s.isBlank() ? def : s;
    }
}
