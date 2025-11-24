package insurance_package.repository;

import insurance_package.model.Quote;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuoteRepository extends MongoRepository<Quote, ObjectId> {
    List<Quote> findByUserId(ObjectId userId);
}
