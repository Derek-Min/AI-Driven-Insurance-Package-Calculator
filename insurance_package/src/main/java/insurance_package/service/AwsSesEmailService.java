package insurance_package.service;

import insurance_package.model.PremiumResult;
import insurance_package.model.Quote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
@Profile("aws")
@RequiredArgsConstructor
public class AwsSesEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${aws.ses.sender-email}")
    private String senderEmail;

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false); // Plain text

            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendQuoteEmail(String toEmail, Quote quote, PremiumResult result) {
        try {
            // Generate HTML content based on quote type
            String htmlContent = emailTemplateService.generateQuoteEmail(quote, result);
            String subject = String.format("Your %s Insurance Quote - %s",
                    quote.getLine(),
                    quote.getQuoteId());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // HTML content

            mailSender.send(message);
            log.info("Quote email sent to {} for quote {}", toEmail, quote.getQuoteId());

        } catch (Exception e) {
            log.error("Failed to send quote email to {}: {}", toEmail, e.getMessage());
            // Fallback to plain text
            sendPlainTextQuoteEmail(toEmail, quote);
        }
    }

    private void sendPlainTextQuoteEmail(String toEmail, Quote quote) {
        String subject = String.format("Your %s Insurance Quote - %s",
                quote.getLine(),
                quote.getQuoteId());

        String body = String.format("""
            Dear %s,
            
            Thank you for requesting a %s insurance quote.
            
            Quote Details:
            - Quote ID: %s
            - Insurance Line: %s
            - Total Premium: %s %s
            - Risk Score: %s
            
            Our team will contact you shortly with more details.
            
            Best regards,
            Trust Insurance Team
            
            Contact Information:
            Phone: +603-1234 5678
            Email: info@trustinsurance.com
            """,
                quote.getCustomerName(),
                quote.getLine(),
                quote.getQuoteId(),
                quote.getLine(),
                quote.getCurrency(),
                quote.getTotalPremium(),
                quote.getRiskScore()
        );

        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendQuoteEmailWithAttachment(String toEmail, String subject,
                                             String body, File attachment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false);

            // Add PDF attachment
            if (attachment != null && attachment.exists()) {
                byte[] fileBytes = Files.readAllBytes(attachment.toPath());
                helper.addAttachment(attachment.getName(),
                        new ByteArrayResource(fileBytes),
                        "application/pdf");
                log.info("Attached PDF: {} ({} bytes)",
                        attachment.getName(),
                        fileBytes.length);
            }

            mailSender.send(message);
            log.info("Email with attachment sent to {}", toEmail);

        } catch (MessagingException | IOException e) {
            log.error("Failed to send email with attachment to {}: {}",
                    toEmail, e.getMessage());
            // Send without attachment
            sendEmail(toEmail, subject, body + "\n\n[Attachment could not be attached]");
        }
    }
}