package insurance_package.model;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("vehicle_profiles")
@Data
public class VehicleProfile {
    @Id
    private ObjectId id;

    @Field("user_id")
    private ObjectId userId;

    @Field("plate_no")
    private String plateNo;

    private String make;
    private String model;
    private int year;
    private String usage;

    @Field("ncd_percent")
    private int ncdPercent;

    private String region;
}
