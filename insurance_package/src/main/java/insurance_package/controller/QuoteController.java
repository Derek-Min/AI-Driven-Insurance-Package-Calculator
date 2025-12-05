package insurance_package.controller;

import insurance_package.model.PremiumResult;
import insurance_package.model.Quote;
import insurance_package.model.QuotationRequest;
import insurance_package.repository.QuoteRepository;
import insurance_package.service.AwsEmailService;
import insurance_package.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final PricingService pricingService;
    private final QuoteRepository quoteRepository;
    private final AwsEmailService emailService;

    @PostMapping("/preview")
    public PremiumResult preview(@RequestBody QuotationRequest req) {
        return pricingService.calculatePremium(req);
    }

    @PostMapping
    public ResponseEntity<Quote> create(@RequestBody QuotationRequest req) {
        PremiumResult result = pricingService.calculatePremium(req);
        Quote quote = new Quote();
        quote.setLine(req.getLine());
        quote.setCreatedAt(Instant.now());
        quote.setTotalPremium(result.getTotalPremium());
        quote.setRiskScore(result.getRiskScore());

        Quote saved = quoteRepository.save(quote);

        // 2) Build email only for Motor line (private or commercial)
        if ("Motor".equalsIgnoreCase(req.getLine())) {
            sendMotorQuoteEmail(req, result);
        }

        return ResponseEntity.ok(saved);
    }

    private void sendMotorQuoteEmail(QuotationRequest req, PremiumResult result) {
        // Extract fields from attributes map safely
        var attrs = req.getAttributes();

        String customerName = String.valueOf(attrs.getOrDefault("customer_name", "Valued Customer"));
        String customerEmail = String.valueOf(attrs.getOrDefault("email", "test@example.com"));
        String make = String.valueOf(attrs.getOrDefault("make", ""));
        String model = String.valueOf(attrs.getOrDefault("model", ""));
        String year = String.valueOf(attrs.getOrDefault("year", ""));
        String plateNo = String.valueOf(attrs.getOrDefault("plate_no", "-"));
        String usage = String.valueOf(attrs.getOrDefault("usage", "Private"));
        String region = String.valueOf(attrs.getOrDefault("region", "-"));
        double ncdPercent = toDouble(attrs.getOrDefault("ncd_percent", 0));

        String vehicleDescription = (make + " " + model + " " + year).trim();

        String insurerName = "Trust Insurance"; // or from config

        String htmlBody = emailService.buildQuotationEmailHtml(
                customerName,
                insurerName,
                vehicleDescription,
                plateNo,
                usage,
                region,
                ncdPercent,
                result
        );

        String subject = String.format(
                "[%s] Motor Insurance Quotation - %s (%s)",
                insurerName,
                plateNo,
                usage
        );

        emailService.sendQuoteEmail(customerEmail, subject, htmlBody);
    }

    private double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quote> byId(@PathVariable String id) {
        return quoteRepository.findById(new ObjectId(id))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Quote> byUser(@PathVariable String userId) {
        return quoteRepository.findByUserId(new ObjectId(userId));
    }
}
