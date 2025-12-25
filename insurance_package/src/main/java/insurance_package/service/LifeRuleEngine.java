package insurance_package.service;

import insurance_package.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LifeRuleEngine {

    @SuppressWarnings("unchecked")
    public PremiumResult applyBusinessRules(QuotationRequest req, Map<String, Object> rates) {

        Map<String, Object> slots = req.getSlots();

        double base = toDouble(rates.getOrDefault("base", 35));

        int age = toInteger(slots.getOrDefault("age", 0));
        boolean smoker = "yes".equalsIgnoreCase(
                String.valueOf(slots.getOrDefault("smoker_status", "no"))
        );
        double income = toDouble(slots.getOrDefault("income", 0));

        // -------- Age band factor --------
        double ageFactor = 1.0;
        List<Map<String, Object>> bands =
                (List<Map<String, Object>>) rates.get("life_age_bands");

        if (bands != null) {
            for (Map<String, Object> band : bands) {
                int min = toInteger(band.get("min"));
                int max = toInteger(band.get("max"));
                if (age >= min && age <= max) {
                    ageFactor = toDouble(band.get("factor"));
                    break;
                }
            }
        }

        // -------- Smoker loading --------
        double smokerLoad = smoker
                ? toDouble(rates.getOrDefault("smoker_load", 1.35))
                : 1.0;

        double basePremium = round2(base * ageFactor * smokerLoad);

        // -------- Income-based scaling --------
        double coverageMultiplier =
                Math.min(2.0, Math.max(1.0, income / 5000.0));

        double totalPremium = round2(basePremium * coverageMultiplier);

        // -------- Risk score --------
        int riskScore = Math.min(100,
                (smoker ? 75 : 45) + (age > 45 ? 10 : 0));

        // =================================================
        // ✅ BUILD PREMIUM BREAKDOWN (THIS IS THE KEY FIX)
        // =================================================
        PremiumBreakdown breakdown = new PremiumBreakdown();
        breakdown.setCurrency("MYR");

        List<CoverageItem> items = new ArrayList<>();

// Base Life Premium
        CoverageItem baseItem = new CoverageItem();
        baseItem.setCode("BASE_LIFE");
        baseItem.setLabel("Base Life Premium");
        baseItem.setAmount(basePremium);
        items.add(baseItem);

// Critical Illness Rider (optional)
        if (Boolean.TRUE.equals(slots.get("CRITICAL_ILLNESS_enabled"))) {
            double ciPremium = round2(basePremium * 0.35);

            CoverageItem ciItem = new CoverageItem();
            ciItem.setCode("CRITICAL_ILLNESS");
            ciItem.setLabel("Critical Illness Rider");
            ciItem.setAmount(ciPremium);
            items.add(ciItem);
        }

        breakdown.setItems(items);
        breakdown.setTotalPremium(totalPremium);
        breakdown.setCurrency("MYR");
        breakdown.setSummaryExplanation(
                "Premium calculated based on age, income, smoker status, and selected riders."
        );


        // =================================================
        // RETURN RESULT
        // =================================================
        PremiumResult result = new PremiumResult();
        result.setBasePremium(basePremium);
        result.setTotalPremium(totalPremium);
        result.setRiskScore(riskScore);
        result.setBreakdown(breakdown);

        return result;
    }

    // ---------------- helpers ----------------

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        return (o instanceof Number n)
                ? n.doubleValue()
                : Double.parseDouble(o.toString());
    }

    private static int toInteger(Object o) {
        if (o == null) return 0;
        return (o instanceof Number n)
                ? n.intValue()
                : Integer.parseInt(o.toString());
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
