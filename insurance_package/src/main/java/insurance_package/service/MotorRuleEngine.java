package insurance_package.service;

import insurance_package.model.PremiumResult;
import insurance_package.model.QuotationRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class MotorRuleEngine {

    @SuppressWarnings("unchecked")
    public PremiumResult applyBusinessRules(QuotationRequest req, Map<String, Object> rates) {
        double base = toDouble(rates.getOrDefault("base", 400));
        double perYear = toDouble(rates.getOrDefault("per_year", 20));

        int year = toInt(req.getAttributes().get("year"));
        int vehicleAge = LocalDate.now().getYear() - year;

        String usage = String.valueOf(req.getAttributes().get("usage"));
        String region = String.valueOf(req.getAttributes().get("region"));
        double ncd = toDouble(req.getAttributes().get("ncd_percent"));

        // Get usage and region factors from MongoDB product.base_rates JSON
        double usageFactor = getOr((Map<String, Object>) rates.get("usage_factors"), usage, 1.0);
        double regionFactor = getOr((Map<String, Object>) rates.get("region_factors"), region, 1.0);

        // Core premium calculations
        double ageLoad = perYear * Math.max(0, vehicleAge);
        double basePremium = base + ageLoad;
        double discount = 1 - (ncd / 100.0);

        double subtotal = basePremium * usageFactor * regionFactor * discount;

        // Add taxes and fees
        double sst = subtotal * 0.06;   // 6% SST
        double stampDuty = 10.0;
        double total = round2(subtotal + sst + stampDuty);

        // Risk score between 0–100 (simplified logic)
        int risk = Math.max(0, Math.min(100,
                50 + (vehicleAge * 2) + ("Kuala Lumpur".equalsIgnoreCase(region) ? 8 : 0) - (int) (ncd / 2)));

        // Build PremiumResult
        PremiumResult r = new PremiumResult();
        r.setBasePremium(round2(basePremium));
        r.setTotalPremium(total);
        r.setRiskScore(risk);
        r.setCoverages(List.of("Third Party Liability", "Accident", "Fire/Theft"));
        r.setAppliedRules(List.of("AgeLoad", "UsageFactor", "RegionFactor", "NCD", "SST", "StampDuty"));

        return r;
    }


    private static double toDouble(Object o){
        return o instanceof Number n ? n.doubleValue() : Double.parseDouble(o.toString());
    }

    private static int toInt(Object o){
        return o instanceof Number n ? n.intValue() : Integer.parseInt(o.toString());
    }

    private static double getOr(Map<String, Object> m, String k, double d){
        if (m == null) return d;
        Object v = m.get(k);
        return v == null ? d : toDouble(v);
    }
    private static double round2(double v){
        return Math.round(v * 100.0) / 100.0;
    }
}
