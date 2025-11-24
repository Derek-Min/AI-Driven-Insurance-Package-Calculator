package insurance_package.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AwsChatbotService {

    private final LambdaClient lambdaClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${aws.lambda.chatbot.name}")
    private String functionName;

    public Map<String, Object> askChatbot(Map<String, Object> payload) {
        try {
            byte[] jsonBytes = mapper.writeValueAsBytes(payload);

            InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(SdkBytes.fromByteArray(jsonBytes))
                    .build();

            InvokeResponse response = lambdaClient.invoke(request);
            String json = response.payload().asUtf8String();

            return mapper.readValue(json, Map.class);

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}
