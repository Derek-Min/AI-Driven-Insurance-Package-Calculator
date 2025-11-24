package insurance_package.model;

import lombok.Data;
import java.util.Map;

@Data
public class QuotationRequest {
    private String line;
    private Map<String, Object> attributes;
}
