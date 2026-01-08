package insurance_package.mongo.repository;

import insurance_package.model.CoverageOption;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CoverageOptionRepository extends MongoRepository<CoverageOption, ObjectId> {
    List<CoverageOption> findByProductId(ObjectId productId);
}
