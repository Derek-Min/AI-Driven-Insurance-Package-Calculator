package insurance_package.service;

import insurance_package.model.CoverageItem;
import insurance_package.model.PremiumBreakdown;
import insurance_package.model.PremiumResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AwsEmailService {

    private static final Logger log = LoggerFactory.getLogger(AwsEmailService.class);

    private final SesClient sesClient;

    @Value("${aws.ses.fromAddress:no-reply@example.com}")
    private String fromAddress;

    // =======================
    // SEND EMAIL
    // =======================
    public void sendQuoteEmail(String to, String subject, String htmlBody) {
        try {
            Destination dest = Destination.builder()
                    .toAddresses(to)
                    .build();

            Message message = Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                            .html(Content.builder().data(htmlBody).build())
                            .build())
                    .build();

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromAddress)
                    .destination(dest)
                    .message(message)
                    .build();

            sesClient.sendEmail(request);

            log.info("SES email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send SES email to {}: {}", to, e.getMessage());
        }
    }

    // =======================
    // Build Coverage Table Rows
    // =======================
    private String buildCoverageRows(PremiumBreakdown breakdown) {
        StringBuilder sb = new StringBuilder();
        for (CoverageItem item : breakdown.getItems()) {
            sb.append("<tr>")
                    .append("<td>").append(item.getLabel()).append("</td>")
                    .append("<td>").append(String.format("%.2f", item.getAmount())).append("</td>")
                    .append("</tr>");
        }
        return sb.toString();
    }

    // =======================
    // Load HTML Template
    // =======================
    private String loadTemplate(String filename) {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/" + filename)) {

            if (is == null) {
                throw new IllegalStateException("Email template not found: " + filename);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Error reading email template: " + filename, e);
        }
    }


    // =======================
    // Build Full Email HTML
    // =======================
    public String buildQuotationEmailHtml(String customerName,
                                          String insurerName,
                                          String vehicleDescription,
                                          String plateNo,
                                          String usage,
                                          String region,
                                          double ncdPercent,
                                          PremiumResult result) {

        PremiumBreakdown breakdown = result.getBreakdown();

        String template = loadTemplate("quote.html"); // 👈 uses /templates/email/quote.html

        String coverageRows = buildCoverageRows(breakdown);

        return template
                .replace("${customerName}", customerName)
                .replace("${insurerName}", insurerName)
                .replace("${vehicleDescription}", vehicleDescription)
                .replace("${plateNo}", plateNo)
                .replace("${sumInsured}", String.format("%.2f", breakdown.getSumInsured()))
                .replace("${currency}", breakdown.getCurrency())
                .replace("${region}", region)
                .replace("${ncdPercent}", String.format("%.2f", ncdPercent))
                .replace("${coverageRows}", coverageRows)
                .replace("${totalPremium}", String.format("%.2f", breakdown.getTotalPremium()))
                .replace("${contactNumber}", "+95-XXX-XXX-XXX");
    }
}
