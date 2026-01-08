package insurance_package.mongo.repository;

import insurance_package.model.Quote;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuoteRepository extends MongoRepository<Quote, String> {
    // ✅ remove invalid method
}
