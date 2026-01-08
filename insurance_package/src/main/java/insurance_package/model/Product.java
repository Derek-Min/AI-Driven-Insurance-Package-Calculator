package insurance_package.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Document("products")
public class Product {
    @Id
    private ObjectId id;
    private String line;
    private String name;
    private String provider;
    private String description;
    private Boolean active;
    private Map<String, Object> baseRates;
    private Instant createdAt;
}
