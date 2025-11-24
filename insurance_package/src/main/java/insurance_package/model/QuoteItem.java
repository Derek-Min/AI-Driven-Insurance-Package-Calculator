package insurance_package.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document("quote_items")
@Data
public class QuoteItem {
    @Id
    private ObjectId id;

    @Field("quote_id")
    private ObjectId quoteId;

    @Field("product_id")
    private ObjectId productId;

    @Field("product_name")
    private String productName;

    @Field("base_premium")
    private double basePremium;
    private List<String> coverages;

    @Field("suggested_premium")
    private double suggestedPremium;
}
