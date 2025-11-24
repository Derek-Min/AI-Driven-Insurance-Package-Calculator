package insurance_package.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("coverage_options")
@Data
public class CoverageOption {
    @Id
    private ObjectId id;

    @Field("product_id")
    private ObjectId productId;
    private String code;
    private String label;
    private String description;

    @Field("default_on")
    private boolean defaultOn;

    @Field("load_factor")
    private double loadFactor;
}
