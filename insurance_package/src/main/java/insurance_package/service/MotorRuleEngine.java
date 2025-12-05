package insurance_package.service;

import insurance_package.model.CoverageItem;
import insurance_package.model.CoverageOption;
import insurance_package.model.PremiumBreakdown;
import insurance_package.model.PremiumResult;
import insurance_package.model.QuotationRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;

@Component
public class MotorRuleEngine {

    /**
     * New version that also takes coverage options so we can build
     * a detailed breakdown (BASIC, THEFT, SRCC, etc.).
     */
    @SuppressWarnings("unchecked")
    public PremiumResult applyBusinessRules(QuotationRequest req,
                                            Map<String, Object> rates,
                                            List<CoverageOption> coverageOptions) {

        // ----------- 1. Core premium using your existing logic -----------
        double base = toDouble(rates.getOrDefault("base", 400));
        double perYear = toDouble(rates.getOrDefault("per_year", 20));

        int year = toInt(req.getAttributes().get("year"));
        int vehicleAge = LocalDate.now().getYear() - year;

        String usage = String.valueOf(req.getAttributes().get("usage"));
        String region = String.valueOf(req.getAttributes().get("region"));
        double ncd = toDouble(req.getAttributes().getOrDefault("ncd_percent", 0));

        // Optional: sum insured & currency from attributes / base_rates
        double sumInsured = toDouble(req.getAttributes().getOrDefault("sum_insured", 0));
        String currency = String.valueOf(
                rates.getOrDefault("currency", "MMK") // or "MYR"
        );

        Map<String, Object> usageFactors = (Map<String, Object>) rates.get("usage_factors");
        Map<String, Object> regionFactors = (Map<String, Object>) rates.get("region_factors");

        double usageFactor = getOr(usageFactors, usage, 1.0);
        double regionFactor = getOr(regionFactors, region, 1.0);

        // Core premium calculations (same as before)
        double ageLoad = perYear * Math.max(0, vehicleAge);
        double basePremium = base + ageLoad;
        double discount = 1 - (ncd / 100.0);

        double subtotal = basePremium * usageFactor * regionFactor * discount;

        // Taxes and fees
        double sst = subtotal * 0.06;   // 6% SST
        double stampDuty = 10.0;

        // Core motor premium (this will be our BASIC coverage)
        double coreMotorPremium = round2(subtotal + sst + stampDuty);

        // ----------- 2. Build coverage items list -----------
        List<CoverageItem> items = new ArrayList<>();

        // BASIC / CORE coverage item
        CoverageItem basicItem = new CoverageItem();
        basicItem.setCode("BASIC_MOTOR");
        basicItem.setLabel("Motor Basic Premium");
        basicItem.setAmount(coreMotorPremium);
        items.add(basicItem);

        // Additional coverages from coverage_options collection
        if (coverageOptions != null) {
            for (CoverageOption option : coverageOptions) {

                // Decide if this coverage should be applied:
                // - defaultOn=true OR
                // - request contains a flag like "THEFT_enabled" = true
                String flagKey = option.getCode() + "_enabled";
                Object flagVal = req.getAttributes().get(flagKey);
                boolean explicitlyEnabled = (flagVal instanceof Boolean b && b)
                        || "true".equalsIgnoreCase(String.valueOf(flagVal));

                // Skip BASIC here; we already handled it with coreMotorPremium
                boolean isBasic = "BASIC_MOTOR".equalsIgnoreCase(option.getCode())
                        || "BASIC".equalsIgnoreCase(option.getCode());

                if (!isBasic && (option.isDefaultOn() || explicitlyEnabled)) {

                    double amount;
                    // Simple rule:
                    // - If loadFactor < 1 => treat as % of sum insured
                    // - If loadFactor >= 1 => treat as a flat amount
                    if (option.getLoadFactor() < 1.0 && sumInsured > 0) {
                        amount = round2(sumInsured * option.getLoadFactor());
                    } else {
                        amount = round2(option.getLoadFactor());
                    }

                    CoverageItem item = new CoverageItem();
                    item.setCode(option.getCode());
                    item.setLabel(option.getLabel());
                    item.setAmount(amount);

                    items.add(item);
                }
            }
        }

        // Total premium is sum of all items
        double totalPremium = round2(
                items.stream().mapToDouble(CoverageItem::getAmount).sum()
        );

        // ----------- 3. Build risk score (same logic as before) -----------
        int risk = Math.max(0, Math.min(100,
                50
                        + (vehicleAge * 2)
                        + ("Kuala Lumpur".equalsIgnoreCase(region) ? 8 : 0)
                        - (int) (ncd / 2)));

        // ----------- 4. Assemble PremiumBreakdown & PremiumResult -----------

        PremiumBreakdown breakdown = new PremiumBreakdown();
        breakdown.setSumInsured(sumInsured);
        breakdown.setCurrency(currency);
        breakdown.setItems(items);
        breakdown.setTotalPremium(totalPremium);
        breakdown.setSummaryExplanation(
                "Premium is based on vehicle age, usage, region, NCD and selected coverages such as "
                        + items.stream().map(CoverageItem::getLabel).collect(Collectors.joining(", ")) + "."
        );

        PremiumResult result = new PremiumResult();
        result.setBasePremium(round2(basePremium));       // core base before factors
        result.setTotalPremium(totalPremium);
        result.setRiskScore(risk);
        result.setCoverages(
                items.stream().map(CoverageItem::getLabel).collect(Collectors.toList())
        );
        result.setAppliedRules(
                List.of("AgeLoad", "UsageFactor", "RegionFactor", "NCD", "SST", "StampDuty", "CoverageOptions")
        );

        // Quick map version if UI/chatbot wants it
        Map<String, Object> coverageMap = new LinkedHashMap<>();
        for (CoverageItem item : items) {
            coverageMap.put(item.getLabel(), item.getAmount());
        }
        result.setCoverageBreakdown(coverageMap);

        result.setBreakdown(breakdown);

        return result;
    }

    // ----------------- helpers (unchanged) -----------------

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        return (o instanceof Number n) ? n.doubleValue() : Double.parseDouble(o.toString());
    }

    private static int toInt(Object o) {
        if (o == null) return 0;
        return (o instanceof Number n) ? n.intValue() : Integer.parseInt(o.toString());
    }

    private static double getOr(Map<String, Object> m, String k, double d) {
        if (m == null) return d;
        Object v = m.get(k);
        return v == null ? d : toDouble(v);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
