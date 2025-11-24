package insurance_package.model;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumBreakdown {
    private BigDecimal base;
    private Map<String, BigDecimal> adjustments;
    private BigDecimal coverageTotal;
}
