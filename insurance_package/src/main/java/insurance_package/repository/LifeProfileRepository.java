package insurance_package.repository;

import insurance_package.model.LifeProfile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LifeProfileRepository extends MongoRepository<LifeProfile, ObjectId> {
    List<LifeProfile> findByUserId(ObjectId userId);
}
