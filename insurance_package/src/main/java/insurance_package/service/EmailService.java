package insurance_package.service;

import insurance_package.model.PremiumResult;
import insurance_package.model.Quote;

import java.io.File;
import java.util.Map;

public interface EmailService {
    void sendEmail(String toEmail, String subject, String body);
    void sendQuoteEmail(String toEmail, Quote quote, PremiumResult result);
    void sendQuoteEmailWithAttachment(String toEmail, String subject, String body, File attachment);
}