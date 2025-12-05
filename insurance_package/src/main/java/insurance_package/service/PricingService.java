package insurance_package.service;

import insurance_package.exception.PricingException;
import insurance_package.exception.ValidationException;
import insurance_package.model.CoverageOption;
import insurance_package.model.PremiumResult;
import insurance_package.model.Product;
import insurance_package.model.QuotationRequest;
import insurance_package.repository.CoverageOptionRepository;
import insurance_package.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final CoverageOptionRepository coverageOptionRepository;
    private final ProductRepository productRepository;
    private final MotorRuleEngine motorRuleEngine;
    private final LifeRuleEngine lifeRuleEngine;

    public PremiumResult calculatePremium(QuotationRequest req) {
        validate(req);

        Product product = productRepository
                .findFirstByLineAndActive(req.getLine(), true)
                .orElseThrow(() -> new PricingException("No active product for line: " + req.getLine()));

        Map<String, Object> rates = product.getBaseRates();

        // 👇 Default base rates if Mongo document has no base_rates yet
        if (rates == null) {
            rates = Map.of(
                    "base", 400.0,
                    "per_year", 20.0,
                    "currency", "MYR"  // <-- backend currency code for RM
            );
        }

        // Load coverage options for this product
        List<CoverageOption> coverageOptions =
                coverageOptionRepository.findByProductId(product.getId());

        return switch (req.getLine()) {
            case "Motor" -> motorRuleEngine.applyBusinessRules(req, rates, coverageOptions);
            case "Life"  -> lifeRuleEngine.applyBusinessRules(req, rates);
            default      -> throw new PricingException("Unsupported line: " + req.getLine());
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
        // TODO: implement if you want a separate persistence layer
        return null;
    }

    private void must(QuotationRequest req, String key) {
        var attributes = req.getAttributes();
        if (attributes == null || !attributes.containsKey(key) || attributes.get(key) == null)
            throw new ValidationException("Missing required attributes for key: " + key);
    }
}
