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
    // SEND EMAIL (SES)
    // =======================
    public void sendQuoteEmail(String to, String subject, String htmlBody) {

        try {
            Destination destination = Destination.builder()
                    .toAddresses(to)
                    .build();

            Content subjectContent = Content.builder()
                    .data(subject)
                    .charset("UTF-8")
                    .build();

            Content htmlContent = Content.builder()
                    .data(htmlBody)
                    .charset("UTF-8")
                    .build();

            Body body = Body.builder()
                    .html(htmlContent)
                    .build();

            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(body)
                    .build();

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromAddress)
                    .destination(destination)
                    .message(message)
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);

            log.info("SES email sent successfully. MessageId={}", response.messageId());

        } catch (MessageRejectedException e) {
            log.error("SES rejected the message. Verify email addresses. {}", e.awsErrorDetails().errorMessage());
        } catch (SesException e) {
            log.error("SES error: {}", e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending SES email", e);
        }
    }

    // =======================
    // Build Coverage Rows
    // =======================
    private String buildCoverageRows(PremiumBreakdown breakdown) {
        StringBuilder sb = new StringBuilder();
        for (CoverageItem item : breakdown.getItems()) {
            sb.append("<tr>")
                    .append("<td>").append(item.getLabel()).append("</td>")
                    .append("<td style='text-align:right'>")
                    .append(String.format("%.2f", item.getAmount()))
                    .append("</td>")
                    .append("</tr>");
        }
        return sb.toString();
    }

    // =======================
    // Load HTML Template
    // =======================
    private String loadTemplate(String filename) {

        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("templates/" + filename)) {

            if (is == null) {
                throw new IllegalStateException("Email template not found: " + filename);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load email template: " + filename, e);
        }
    }

    // =======================
    // Build Full Quotation Email
    // =======================
    public String buildQuotationEmailHtml(
            String customerName,
            String insurerName,
            String vehicleDescription,
            String plateNo,
            String usage,
            String region,
            double ncdPercent,
            PremiumResult result) {

        PremiumBreakdown breakdown = result.getBreakdown();

        String template = loadTemplate("quote.html");

        return template
                .replace("${customerName}", customerName)
                .replace("${insurerName}", insurerName)
                .replace("${vehicleDescription}", vehicleDescription)
                .replace("${plateNo}", plateNo)
                .replace("${usage}", usage)
                .replace("${region}", region)
                .replace("${sumInsured}", String.format("%.2f", breakdown.getSumInsured()))
                .replace("${currency}", breakdown.getCurrency())
                .replace("${ncdPercent}", String.format("%.2f", ncdPercent))
                .replace("${coverageRows}", buildCoverageRows(breakdown))
                .replace("${totalPremium}", String.format("%.2f", breakdown.getTotalPremium()))
                .replace("${contactNumber}", "+95-XXX-XXX-XXX");
    }
}
