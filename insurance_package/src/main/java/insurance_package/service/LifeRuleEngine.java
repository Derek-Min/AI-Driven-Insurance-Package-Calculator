package insurance_package.service;

import insurance_package.model.PremiumResult;
import insurance_package.model.QuotationRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LifeRuleEngine {

    @SuppressWarnings("unchecked")
    public PremiumResult applyBusinessRules(QuotationRequest req, Map<String, Object> rates) {
        double base = toDouble(rates.getOrDefault("base", 35));

        int age = toInteger(req.getAttributes().get("age"));
        boolean smoker = "Smoker".equalsIgnoreCase(String.valueOf(req.getAttributes().get("smoker_status")));
        double income = toDouble(req.getAttributes().get("income"));

        double ageFactor = 1.0;
        var bands = (List<Map<String,Object>>) rates.get("life_age_bands");
        if (bands != null) {
            for (var band : bands) {
                int min = toInteger(band.get("min"));
                int max = toInteger(band.get("max"));
                if (age >= min && age <= max) {
                    ageFactor = toDouble(band.get("factor"));
                    break;
                }
            }
        }
        double smokerLoad = smoker ? toDouble(rates.getOrDefault("smoker_load", 1.35)) : 1.0;
        double basePremium = base * ageFactor * smokerLoad;

        // simple coverage scaling by income
        double coverageMultiplier = Math.min(2.0, Math.max(1.0, income / 5000.0));
        double total = round2(basePremium * coverageMultiplier);

        int risk = Math.min(100, (smoker ? 75 : 45) + (age > 45 ? 10 : 0));

        PremiumResult r = new PremiumResult();
        r.setBasePremium(round2(basePremium));
        r.setTotalPremium(total);
        r.setRiskScore(risk);
        r.setCoverages(List.of("Death", "TPD", "Critical Illness"));
        r.setAppliedRules(List.of("AgeBand", "SmokerLoad", "IncomeBand"));
        return r;
    }

    private static double toDouble(Object o){ return o instanceof Number n ? n.doubleValue() : Double.parseDouble(o.toString()); }
    private static int toInteger(Object o){ return o instanceof Number n ? n.intValue() : Integer.parseInt(o.toString()); }
    private static double round2(double v){ return Math.round(v * 100.0) / 100.0; }
}
