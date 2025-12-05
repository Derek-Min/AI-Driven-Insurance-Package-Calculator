package insurance_package.model;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumBreakdown {
    private Double sumInsured;
    private String currency;

    private List<CoverageItem> items;

    private Double totalPremium;

    private String summaryExplanation;
}
