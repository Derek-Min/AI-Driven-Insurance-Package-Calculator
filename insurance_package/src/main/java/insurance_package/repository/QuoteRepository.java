package insurance_package.repository;

import insurance_package.model.Quote;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends MongoRepository<Quote, ObjectId> {

    Optional<Quote> findTopByCustomerEmailOrderByCreatedAtDesc(String email);

    Optional<Quote> findByQuoteId(String quoteId);

    List<Quote> findByUserId(ObjectId userId);
}
