package insurance_package.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    private String region;
    private Lambda lambda = new Lambda();

    @Data
    public static class Lambda {
        private String chatbotName;
    }
}
