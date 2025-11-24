package insurance_package.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document("life_profiles")
@Data
public class LifeProfile {
    @Id
    private ObjectId id;

    @Field("user_id")
    private ObjectId userId;

    private int age;
    private String gender;

    @Field("smoker_status")
    private String smokerStatus;
    private String occupation;
    private double income;

    @Field("health_flags")
    private String healthFlags;
}
