package insurance_package.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
@Data
public class User {
    @Id
    private ObjectId id;
    private String fullName;
    private String email;
    private String preferredLanguage;
    private String role;
}
