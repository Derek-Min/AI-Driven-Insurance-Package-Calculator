package insurance_package.service;

import insurance_package.model.*;
import insurance_package.exception.PricingException;
import insurance_package.exception.ValidationException;
import insurance_package.repository.CoverageOptionRepository;
import insurance_package.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final CoverageOptionRepository coverageRepository;
    private final ProductRepository productRepository;
    private final MotorRuleEngine motorRuleEngine;
    private final LifeRuleEngine lifeRuleEngine;

    public PremiumResult calculatePremium(QuotationRequest req) {
        validate(req);

        Product product = productRepository
                .findFirstByLineAndActive(req.getLine(), true)
                .orElseThrow(() -> new PricingException("No active product for line: " + req.getLine()));

        Map<String, Object> rates = product.getBaseRates();
        return switch (req.getLine()) {
            case "Motor" -> motorRuleEngine.applyBusinessRules(req, rates);
            case "Life" -> lifeRuleEngine.applyBusinessRules(req, rates);
            default -> throw new PricingException("Unsupported line: " + req.getLine());
        };
    }

    public void validate(QuotationRequest req) {
        if (req.getLine() == null)
            throw new ValidationException("Line required");

        switch (req.getLine()) {
            case "Motor" -> {
                must(req, "ncd_percent");
                must(req, "region");
                must(req, "usage");
                must(req, "year");
            }
            case "Life" -> {
                must(req, "age");
                must(req, "income");
                must(req, "smoker_status");
            }
            default -> throw new ValidationException("Unsupported line: " + req.getLine());
        }
    }

    public insurance_package.model.Quote persistQuote(QuotationRequest req,
                                                      insurance_package.model.PremiumResult result) {
        return null;
    }

    private void must(QuotationRequest req, String key) {
        var attributes = req.getAttributes();
        if (attributes == null || !attributes.containsKey(key) || attributes.get(key) == null)
            throw new ValidationException("Missing required attributes for key: " + key);
    }
}
