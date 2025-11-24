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
        Quote saved = pricingService.persistQuote(req, result);

        String subject = "Your " + saved.getLine() + " Quote (" + saved.getQuoteId() + ")";
        String body = """
            <h2>Your Quote</h2>
            <p><b>Quote ID:</b> %s</p>
            <p><b>Total Premium:</b> %s %s</p>
            <p><b>Summary:</b> %s</p>
        """.formatted(saved.getQuoteId(), saved.getCurrency(), saved.getTotalPremium(), saved.getExplanation());
        emailService.sendQuoteEmail(saved.getCustomerEmail(), subject, body);

        return ResponseEntity.ok(saved);
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
