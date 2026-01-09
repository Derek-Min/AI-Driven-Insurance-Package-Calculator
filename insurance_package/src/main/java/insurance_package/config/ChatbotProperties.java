package insurance_package.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ChatbotProperties {

    /**
     * Mode: local | aws
     */
    private String mode;

    /**
     * Local chatbot endpoint
     * Example: http://localhost:5000/chatbot
     */
    private String localUrl;
}
