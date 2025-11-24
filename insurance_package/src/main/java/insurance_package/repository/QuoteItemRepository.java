package insurance_package.repository;

import insurance_package.model.QuoteItem;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuoteItemRepository extends MongoRepository<QuoteItem, ObjectId> {
    List<QuoteItem> findByQuoteId(ObjectId quoteId);
}
