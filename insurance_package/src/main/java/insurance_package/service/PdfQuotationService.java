package insurance_package.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import insurance_package.model.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class PdfQuotationService {

    private final SpringTemplateEngine templateEngine;

    @Value("${quotation.pdf.output-dir:./generated-pdfs}")
    private String outputDir;

    @Value("${company.logo.base64:}")
    private String companyLogoBase64;

    public PdfQuotationService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public File generateQuotationPdf(Quote quote) {
        log.info("Generating PDF for quote: {}", quote.getQuoteId());

        try {
            String line = safe(quote.getLine());
            boolean isMotor = "Motor".equalsIgnoreCase(line);

            // Prepare Thymeleaf Context
            Context ctx = new Context();

            // Basic quote information
            ctx.setVariable("quote", quote);
            ctx.setVariable("quotationNo", safe(quote.getQuoteId()));
            ctx.setVariable("issueDate", LocalDate.now());
            ctx.setVariable("validUntil", LocalDate.now().plusDays(14));
            ctx.setVariable("formattedDate",
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

            ctx.setVariable("customerName", safe(quote.getCustomerName()));
            ctx.setVariable("customerEmail", safe(quote.getCustomerEmail()));
            ctx.setVariable("currency", safeOrDefault(quote.getCurrency(), "MYR"));
            ctx.setVariable("totalPremium", quote.getTotalPremium() == null ? 0 : quote.getTotalPremium());
            ctx.setVariable("riskScore", quote.getRiskScore());
            ctx.setVariable("line", line);

            // Company logo
            if (companyLogoBase64 != null && !companyLogoBase64.isBlank()) {
                String logo = companyLogoBase64.trim();
                if (!logo.startsWith("data:image")) {
                    logo = "data:image/png;base64," + logo;
                }
                ctx.setVariable("logoBase64", logo);
            } else {
                ctx.setVariable("logoBase64", "");
            }

            // Request details (slots)
            Map<String, Object> requestDetails = quote.getRequestDetails();
            if (requestDetails == null) {
                requestDetails = new HashMap<>();
            }
            ctx.setVariable("requestDetails", requestDetails);

            // Motor-specific variables - FIXED: Use explicit type casting
            if (isMotor) {
                ctx.setVariable("vehicleMake", safe(requestDetails.get("make")));
                ctx.setVariable("vehicleModel", safe(requestDetails.get("model")));
                ctx.setVariable("vehicleYear", safe(requestDetails.get("year")));
                ctx.setVariable("plateNo", safe(requestDetails.get("plate_no")));
                ctx.setVariable("usage", safe(requestDetails.get("usage")));
                ctx.setVariable("region", safe(requestDetails.get("region")));
                ctx.setVariable("sumInsured", safe(requestDetails.get("sum_insured")));
            }
            // Life-specific variables - FIXED: Proper handling without getOrDefault
            else {
                Map<String, Object> profile = new HashMap<>();

                // Manually check and convert each field
                Object ageObj = requestDetails.get("age");
                profile.put("age", ageObj != null ? String.valueOf(ageObj) : "N/A");

                Object genderObj = requestDetails.get("gender");
                profile.put("gender", genderObj != null ? String.valueOf(genderObj) : "Not specified");

                Object smokerObj = requestDetails.get("smoker_status");
                profile.put("smoker_status", smokerObj != null ? String.valueOf(smokerObj) : "N/A");

                Object incomeObj = requestDetails.get("income");
                profile.put("income", incomeObj != null ? String.valueOf(incomeObj) : "0");

                Object occupationObj = requestDetails.get("occupation");
                profile.put("occupation", occupationObj != null ? String.valueOf(occupationObj) : "Not specified");

                Object maritalObj = requestDetails.get("marital_status");
                profile.put("marital_status", maritalObj != null ? String.valueOf(maritalObj) : "Not specified");

                ctx.setVariable("profile", profile);
            }

            // Premium breakdown
            Map<String, Object> breakdown = quote.getPremiumBreakdown();
            if (breakdown == null) {
                breakdown = new HashMap<>();
            }
            ctx.setVariable("breakdown", breakdown);

            // Base premium
            Object basePremium = breakdown.get("basePremium");
            if (basePremium == null) basePremium = breakdown.get("base_premium");
            ctx.setVariable("basePremium", basePremium == null ? 0 : basePremium);

            // Currency from breakdown (if present)
            Object bdCurrency = breakdown.get("currency");
            if (bdCurrency != null && !String.valueOf(bdCurrency).isBlank()) {
                ctx.setVariable("currency", String.valueOf(bdCurrency));
            }

            // Total premium from breakdown (if present)
            Object total = breakdown.get("totalPremium");
            if (total != null) {
                ctx.setVariable("totalPremium", total);
            }

            // Coverage items
            List<Map<String, Object>> coverageItems = extractCoverageItems(breakdown);
            ctx.setVariable("coverageItems", coverageItems);
            log.debug("PDF coverageItems count = {}", coverageItems.size());

            // Select template - CORRECTED PATH
            String templatePath;
            if (isMotor) {
                templatePath = "pdf/motor-quote"; // Looks for templates/pdf/motor-quote.html
            } else {
                templatePath = "pdf/life-quote";  // Looks for templates/pdf/life-quote.html
            }

            String html = templateEngine.process(templatePath, ctx);

            // Ensure output directory exists
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists()) {
                boolean created = outputDirFile.mkdirs();
                if (!created) {
                    throw new RuntimeException("Failed to create output directory: " + outputDir);
                }
            }

            // Generate PDF file
            String fileName = "Quotation_" + quote.getQuoteId() + ".pdf";
            File pdfFile = new File(outputDirFile, fileName);

            try (OutputStream os = new FileOutputStream(pdfFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();
            }

            log.info("PDF generated successfully: {}", pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (Exception e) {
            log.error("PDF generation failed for quote {}: {}",
                    quote.getQuoteId(), e.getMessage(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    private List<Map<String, Object>> extractCoverageItems(Map<String, Object> breakdown) {
        List<Map<String, Object>> coverageItems = new ArrayList<>();

        Object itemsObj = breakdown.get("items");
        if (itemsObj instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> coverageItem = new HashMap<>();

                if (item instanceof Map) {
                    // Handle Map structure - Use raw types to avoid casting issues
                    Map itemMap = (Map) item;

                    Object labelObj = itemMap.get("label");
                    Object amountObj = itemMap.get("amount");
                    Object descObj = itemMap.get("description");

                    coverageItem.put("label", labelObj != null ? String.valueOf(labelObj) : "");
                    coverageItem.put("amount", amountObj != null ? amountObj : 0);
                    coverageItem.put("description", descObj != null ? String.valueOf(descObj) : "");
                } else {
                    // Try reflection for POJO
                    try {
                        Object labelObj = item.getClass().getMethod("getLabel").invoke(item);
                        Object amountObj = item.getClass().getMethod("getAmount").invoke(item);
                        Object descObj = item.getClass().getMethod("getDescription").invoke(item);

                        coverageItem.put("label", labelObj != null ? labelObj.toString() : "");
                        coverageItem.put("amount", amountObj != null ? amountObj : 0);
                        coverageItem.put("description", descObj != null ? descObj.toString() : "");
                    } catch (Exception e) {
                        // Skip unknown type
                        log.warn("Could not extract coverage item: {}", item);
                    }
                }

                if (!coverageItem.isEmpty()) {
                    coverageItems.add(coverageItem);
                }
            }
        }

        return coverageItems;
    }

    private String safe(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private String safeOrDefault(Object o, String def) {
        String s = safe(o);
        return s == null || s.isBlank() ? def : s;
    }
}