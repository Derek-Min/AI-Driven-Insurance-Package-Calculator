package insurance_package.model;

import lombok.Data;
import java.util.List;

@Data
public class PremiumResult {
    private double basePremium;
    private double totalPremium;
    private int riskScore;
    private String quoteId;  // ADD THIS FIELD - Make sure it exists
    private List<String> coverages;
    private List<String> appliedRules;
    private PremiumBreakdown breakdown;

    // Add getter and setter if not using Lombok properly
    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }
}