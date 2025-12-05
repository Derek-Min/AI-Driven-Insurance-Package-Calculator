package insurance_package.model;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Document("quotes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Quote {
    @Id
    private ObjectId id;

    private ObjectId userId;       // <-- aligns with repository method
    private String quoteId;        // Q-YYYYMMDD-###
    private String line;           // Motor | Life
    private String currency;       // MYR

    private String customerName;
    private String customerEmail;

    private Map<String, Object> requestDetails;
    private Map<String, Object> premiumBreakdown;
    private Double totalPremium;
    private int riskScore;
    private String explanation;

    private QuoteStatus status;
    private Instant createdAt;
}
