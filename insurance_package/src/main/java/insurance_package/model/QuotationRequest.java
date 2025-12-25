package insurance_package.model;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationRequest {

    // "Life" or "Motor"
    private String line;

    private String customerName;
    private String email;

    // chatbot collected values
    private Map<String, Object> slots;
}
