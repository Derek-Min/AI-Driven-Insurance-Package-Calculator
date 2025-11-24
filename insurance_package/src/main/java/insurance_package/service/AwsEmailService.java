// src/main/java/insurance_package/service/AwsEmailService.java
package insurance_package.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Service
@RequiredArgsConstructor
public class AwsEmailService {

    private static final Logger log = LoggerFactory.getLogger(AwsEmailService.class);

    // Injected from AwsConfig (SesClient bean)
    private final SesClient sesClient;

    @Value("${aws.ses.fromAddress:no-reply@example.com}")
    private String fromAddress;

    public void sendQuoteEmail(String to, String subject, String htmlBody) {
        try {
            Destination dest = Destination.builder()
                    .toAddresses(to)
                    .build();

            Message msg = Message.builder()
                    .subject(Content.builder().data(subject).build())
                    .body(Body.builder()
                            .html(Content.builder().data(htmlBody).build())
                            .build())
                    .build();

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromAddress)
                    .destination(dest)
                    .message(msg)
                    .build();

            sesClient.sendEmail(request);

            log.info("SES email sent to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send SES email to {}. Subject: {}. Body:\n{}\nError: {}",
                    to, subject, htmlBody, e.getMessage(), e);
        }
    }
}
