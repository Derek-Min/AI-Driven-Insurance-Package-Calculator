package insurance_package.controller;

import insurance_package.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@Profile("aws")   // only available in AWS
@RestController
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @GetMapping("/api/test/email")
    public String testEmail(@RequestParam String to) {

        File dummyPdf = new File("generated-pdfs/test.pdf");

        emailService.sendQuoteEmailWithAttachment(
                to,
                "SES Test Email - Trust Insurance",
                "<h2>SES is configured correctly</h2>",
                dummyPdf
        );

        return "✅ Test email triggered for " + to;
    }
}
