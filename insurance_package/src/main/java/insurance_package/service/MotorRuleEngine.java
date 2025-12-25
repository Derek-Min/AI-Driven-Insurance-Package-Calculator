package insurance_package.service;

import insurance_package.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MotorRuleEngine {

    @SuppressWarnings("unchecked")
    public PremiumResult applyBusinessRules(QuotationRequest req,
                                            Map<String, Object> rates,
                                            List<CoverageOption> coverageOptions) {

        Map<String, Object> slots = req.getSlots();

        // -------- 1. Core premium --------
        double base = toDouble(rates.getOrDefault("base", 400));
        double perYear = toDouble(rates.getOrDefault("per_year", 20));

        int year = toInt(slots.getOrDefault("year", LocalDate.now().getYear()));
        int vehicleAge = LocalDate.now().getYear() - year;

        String usage = String.valueOf(slots.getOrDefault("usage", ""));
        String region = String.valueOf(slots.getOrDefault("region", ""));
        double ncd = toDouble(slots.getOrDefault("ncd_percent", 0));

        double sumInsured = toDouble(slots.getOrDefault("sum_insured", 0));
        String currency = String.valueOf(
                rates.getOrDefault("currency", "MYR")
        );

        Map<String, Object> usageFactors =
                (Map<String, Object>) rates.get("usage_factors");
        Map<String, Object> regionFactors =
                (Map<String, Object>) rates.get("region_factors");

        double usageFactor = getOr(usageFactors, usage, 1.0);
        double regionFactor = getOr(regionFactors, region, 1.0);

        double ageLoad = perYear * Math.max(0, vehicleAge);
        double basePremium = base + ageLoad;
        double discount = 1 - (ncd / 100.0);

        double subtotal =
                basePremium * usageFactor * regionFactor * discount;

        double sst = subtotal * 0.06;
        double stampDuty = 10.0;

        double coreMotorPremium =
                round2(subtotal + sst + stampDuty);

        // -------- 2. Coverage items --------
        List<CoverageItem> items = new ArrayList<>();

        CoverageItem basic = new CoverageItem();
        basic.setCode("BASIC_MOTOR");
        basic.setLabel("Motor Basic Premium");
        basic.setAmount(coreMotorPremium);
        items.add(basic);

        if (coverageOptions != null) {
            for (CoverageOption option : coverageOptions) {

                boolean isBasic =
                        "BASIC".equalsIgnoreCase(option.getCode())
                                || "BASIC_MOTOR".equalsIgnoreCase(option.getCode());

                String flagKey = option.getCode() + "_enabled";
                Object flagVal = slots.get(flagKey);

                boolean enabled =
                        option.isDefaultOn()
                                || Boolean.TRUE.equals(flagVal);


                if (!isBasic && enabled) {
                    double amount;

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

        double totalPremium = round2(
                items.stream().mapToDouble(CoverageItem::getAmount).sum()
        );

        // -------- 3. Risk score --------
        int risk = Math.max(0, Math.min(100,
                50
                        + (vehicleAge * 2)
                        + ("Kuala Lumpur".equalsIgnoreCase(region) ? 8 : 0)
                        - (int) (ncd / 2)));

        // -------- 4. Breakdown --------
        PremiumBreakdown breakdown = new PremiumBreakdown();
        breakdown.setSumInsured(sumInsured);
        breakdown.setCurrency(currency);
        breakdown.setItems(items);
        breakdown.setTotalPremium(totalPremium);
        breakdown.setSummaryExplanation(
                "Premium based on vehicle age, usage, region, NCD and selected coverages: "
                        + items.stream()
                        .map(CoverageItem::getLabel)
                        .collect(Collectors.joining(", "))
        );

        PremiumResult result = new PremiumResult();
        result.setBasePremium(round2(basePremium));
        result.setTotalPremium(totalPremium);
        result.setRiskScore(risk);
        result.setCoverages(
                items.stream()
                        .map(CoverageItem::getLabel)
                        .collect(Collectors.toList())
        );
        result.setAppliedRules(List.of(
                "AgeLoad",
                "UsageFactor",
                "RegionFactor",
                "NCD",
                "SST",
                "StampDuty",
                "CoverageOptions"
        ));

        Map<String, Object> coverageMap = new LinkedHashMap<>();
        for (CoverageItem item : items) {
            coverageMap.put(item.getLabel(), item.getAmount());
        }
        result.setCoverageBreakdown(coverageMap);
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

    private static int toInt(Object o) {
        if (o == null) return 0;
        return (o instanceof Number n)
                ? n.intValue()
                : Integer.parseInt(o.toString());
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
