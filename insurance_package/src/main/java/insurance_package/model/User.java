package insurance_package.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String objectId;

    // You have BOTH _id and an "id" field in Mongo. Map the "id" to a separate property:
    @Field("id")
    private String externalId;

    @Field("email")
    private String email;

    @Field("password_hash")
    private String passwordHash;

    @Field("full_name")
    private String fullName;

    @Field("preferred_language")
    private String preferredLanguage;

    @Field("takaful_preference")
    private Boolean takafulPreference;

    @Field("created_at")
    private LocalDateTime createdAt;
}
