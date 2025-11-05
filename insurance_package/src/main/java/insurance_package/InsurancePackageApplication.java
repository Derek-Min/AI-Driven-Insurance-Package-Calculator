package insurance_package;

import java.util.*;
import insurance_package.model.User;
import insurance_package.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "insurance_package.repository")
public class InsurancePackageApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsurancePackageApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            System.out.println("Insurance Package Application has been initialized");
            System.out.println("All users in Database: ");
            userRepository.findAll().forEach(user1 ->
                    System.out.println("- " + user1.getFullName() + " | " +  user1.getPreferredLanguage()));

            User found = userRepository.findByFullName("Test User");
            System.out.println("Find by Full Name: "+ (found != null ? found.getEmail() : "Not Found!"));


        };
    }

}
