package insurance_package.service;

import insurance_package.model.PremiumResult;
import insurance_package.model.Quote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@Profile("mongo")
@RequiredArgsConstructor
public class NoOpEmailService implements EmailService {

    private final EmailTemplateService emailTemplateService;

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        log.info("=".repeat(60));
        log.info("[NoOpEmailService] SIMULATING EMAIL SEND");
        log.info("To: {}", toEmail);
        log.info("Subject: {}", subject);
        log.info("Body length: {} characters", body.length());
        log.info("=".repeat(60));
    }

    @Override
    public void sendQuoteEmail(String toEmail, Quote quote, PremiumResult result) {
        log.info("=".repeat(60));
        log.info("[NoOpEmailService] SIMULATING QUOTE EMAIL");
        log.info("To: {}", toEmail);
        log.info("Customer: {}", quote.getCustomerName());
        log.info("Quote ID: {}", quote.getQuoteId());
        log.info("Insurance Line: {}", quote.getLine());
        log.info("Total Premium: {} {}", quote.getCurrency(), quote.getTotalPremium());

        try {
            // Generate HTML email content
            String htmlContent = emailTemplateService.renderLifeQuotationEmail(
                    quote.getRequestDetails(),
                    result
            );
            log.info("HTML Email generated ({} chars)", htmlContent.length());
        } catch (Exception e) {
            log.warn("Could not generate HTML email: {}", e.getMessage());
        }

        log.info("=".repeat(60));
    }

    @Override
    public void sendQuoteEmailWithAttachment(String toEmail, String subject, String body, File attachment) {
        log.info("=".repeat(60));
        log.info("[NoOpEmailService] SIMULATING EMAIL WITH ATTACHMENT");
        log.info("To: {}", toEmail);
        log.info("Subject: {}", subject);
        log.info("Attachment: {} ({} bytes)",
                attachment.getName(),
                attachment.length());
        log.info("=".repeat(60));
    }
}