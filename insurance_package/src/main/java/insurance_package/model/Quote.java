package insurance_package.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Builder
@Data
@Document("quotes")
public class Quote {

    @Id
    private String id;

    private String quoteId;
    private String line;
    private String currency;
    private String customerName;
    private String customerEmail;

    // Chatbot slots / request data
    private Map<String, Object> requestDetails;

    // 🔥 MUST stay Map for MongoDB
    private Map<String, Object> premiumBreakdown;

    private Double totalPremium;
    private Integer riskScore;
    private String explanation;
    private QuoteStatus status;
    private Instant createdAt;
}
