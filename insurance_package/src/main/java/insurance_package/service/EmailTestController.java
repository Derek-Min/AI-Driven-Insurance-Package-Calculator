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

    @GetMapping("/api/email/test")
    public String testEmail(@RequestParam String to) {
        awsEmailService.sendQuoteEmail(
                to,
                "Test SES Email",
                "<h1>Hello from your Insurance App</h1><p>SES is working!</p>"
        );
        return "Email sent to " + to;
    }
}
