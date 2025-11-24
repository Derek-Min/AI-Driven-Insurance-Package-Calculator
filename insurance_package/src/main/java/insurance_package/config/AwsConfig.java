package insurance_package.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsConfig {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.ses.fromAddress:minthantwai.mr@gmail.com}")
    private String fromAddress;

    @Bean
    public StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }

    @Bean
    public SesClient sesClient(StaticCredentialsProvider provider) {
        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(provider)
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

    @Bean
    public LambdaClient lambdaClient(StaticCredentialsProvider provider) {
        return LambdaClient.builder()
                .region(Region.of(region))
                .credentialsProvider(provider)
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }
}
