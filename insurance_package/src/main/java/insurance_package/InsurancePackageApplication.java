package insurance_package;

import insurance_package.config.AppProperties;
import insurance_package.mongo.repository.ProductRepository;
import insurance_package.mongo.repository.QuoteRepository;
import insurance_package.mongo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "insurance_package.mongo.repository")
@EnableConfigurationProperties(AppProperties.class)
@ConfigurationPropertiesScan(basePackages = "insurance_package.config")
public class InsurancePackageApplication {

    private static final Logger log =
            LoggerFactory.getLogger(InsurancePackageApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(InsurancePackageApplication.class, args);
    }

    /**
     * Runs on startup (local & mongo)
     */
    @Bean
    CommandLineRunner init(
            UserRepository users,
            ProductRepository products,
            QuoteRepository quotations
    ) {
        return args -> {
            log.info("Insurance Package Application Initialized");
            log.info("Users count    : {}", users.count());
            log.info("Products count : {}", products.count());
            log.info("Quotes count   : {}", quotations.count());
        };
    }
}
