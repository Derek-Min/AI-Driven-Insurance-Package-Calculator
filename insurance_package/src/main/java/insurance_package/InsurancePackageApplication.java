package insurance_package;


import insurance_package.repository.UserRepository;
import insurance_package.repository.ProductRepository;
import insurance_package.repository.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InsurancePackageApplication {

    private static final Logger log = LoggerFactory.getLogger(InsurancePackageApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(InsurancePackageApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository users,
                           ProductRepository products,
                           QuoteRepository quotations) {
        return args -> {
            log.info("Insurance Package Application Initialized");

            log.info("Users:");
            users.findAll().forEach(u -> log.info("- {}", u));   // no getters

            log.info("Products:");
            products.findAll().forEach(p -> log.info("- {}", p)); // no getters

            log.info("Quotations:");
            quotations.findAll().forEach(q -> log.info("- {}", q)); // no getters

            log.info("Data printed successfully!");
        };
    }

}
