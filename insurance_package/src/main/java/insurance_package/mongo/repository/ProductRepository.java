package insurance_package.mongo.repository;

import insurance_package.model.Product;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    List<Product> findByLineAndActive(String line, boolean active);
    Optional<Product> findFirstByLineAndActive(String line, boolean active);
}
