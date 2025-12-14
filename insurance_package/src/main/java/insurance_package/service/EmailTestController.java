package insurance_package.controller;

import insurance_package.service.AwsEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailTestController {

    private final AwsEmailService awsEmailService;

    // TEST ONLY - DO NOT EXPOSE IN PRODUCTION
    @GetMapping("/api/test/email")
    public String testEmail(@RequestParam String to) {

        awsEmailService.sendQuoteEmail(
                to,
                "SES Test Email - Trust Insurance",
                "<h2>SES is configured correctly</h2>" +
                        "<p>This is a test email from the Trust Insurance system.</p>"
        );

        return "Test email sent to " + to;
    }
}

