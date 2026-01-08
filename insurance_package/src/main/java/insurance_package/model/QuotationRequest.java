package insurance_package.model;

import lombok.Data;
import java.util.Map;

@Data
public class QuotationRequest {
    private String line;
    private String customerName;
    private String email;
    private Map<String, Object> slots;
}
