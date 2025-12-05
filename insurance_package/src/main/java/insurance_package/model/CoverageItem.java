package insurance_package.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CoverageItem {
    private String code;
    private String label;
    private Double amount;
}
