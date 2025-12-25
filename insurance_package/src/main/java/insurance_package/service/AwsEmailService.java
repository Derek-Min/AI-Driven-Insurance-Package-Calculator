package insurance_package.service;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class AwsEmailService {

    private static final Logger log = LoggerFactory.getLogger(AwsEmailService.class);

    private final SesClient sesClient;

    @Value("${aws.ses.fromAddress:no-reply@trustinsurance.com}")
    private String fromAddress;

    // -----------------------
    // Simple HTML email
    // -----------------------
    public void sendQuoteEmail(String to, String subject, String htmlBody) {

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

        Body body = Body.builder().html(htmlContent).build();

        software.amazon.awssdk.services.ses.model.Message sesMessage =
                software.amazon.awssdk.services.ses.model.Message.builder()
                        .subject(subjectContent)
                        .body(body)
                        .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .source(fromAddress)
                .destination(destination)
                .message(sesMessage)
                .build();

        sesClient.sendEmail(request);
    }

    // -----------------------
    // Email with PDF attachment
    // -----------------------
    public void sendQuoteEmailWithAttachment(
            String to,
            String subject,
            String htmlBody,
            File pdfFile
    ) {
        try {
            MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);

            // Body
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");

            // Attachment
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(pdfFile);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);
            multipart.addBodyPart(attachment);

            message.setContent(multipart);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            message.writeTo(baos);

            SendRawEmailRequest request = SendRawEmailRequest.builder()
                    .rawMessage(RawMessage.builder()
                            .data(SdkBytes.fromByteArray(baos.toByteArray()))
                            .build())
                    .build();

            sesClient.sendRawEmail(request);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

}
