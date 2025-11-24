package insurance_package.repository;

import insurance_package.model.VehicleProfile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleProfileRepository extends MongoRepository<VehicleProfile, ObjectId> {
    List<VehicleProfile> findByUserId(ObjectId userId);
}
