package insurance_package.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "quotes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {

    @Id
    private ObjectId id;

    private ObjectId userId;          // optional (chatbot user)
    private String quoteId;           // Q-YYYYMMDD-XXX
    private String line;              // Motor | Life
    private String currency;          // MYR

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
