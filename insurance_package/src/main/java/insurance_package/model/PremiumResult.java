package insurance_package.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PremiumResult {
    private double basePremium;
    private double totalPremium;
    private int riskScore;
    private List<String> coverages;
    private List<String> appliedRules;
    private Map<String, Object> coverageBreakdown;
    private PremiumBreakdown breakdown;

}
