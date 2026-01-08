package insurance_package.model;

import lombok.Data;
import java.util.List;

@Data
public class PremiumBreakdown {
    private String currency;
    private double sumInsured;
    private double totalPremium;
    private String summaryExplanation;
    private List<?> items;
}
