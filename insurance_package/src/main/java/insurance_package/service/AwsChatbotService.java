package insurance_package.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AwsChatbotService {

    private final LambdaClient lambdaClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${chatbot.mode:local}")
    private String chatbotMode;   // local | aws

    @Value("${chatbot.lambda.local-url:http://localhost:3001/chatbot}")
    private String localLambdaUrl;

    @Value("${aws.lambda.chatbot.name:}")
    private String functionName;

    public AwsChatbotService(LambdaClient lambdaClient) {
        this.lambdaClient = lambdaClient;
    }

    // =====================================================
    // MAIN ENTRY
    // =====================================================
    @SuppressWarnings("unchecked")
    public Map<String, Object> askChatbot(Map<String, Object> payload) {

        log.error("🔥 CHATBOT MODE = {}", chatbotMode);

        try {
            if ("local".equalsIgnoreCase(chatbotMode)) {
                return callLocalLambda(payload);
            } else {
                return callAwsLambda(payload);
            }
        } catch (Exception e) {
            log.error("Chatbot error", e);
            return Map.of(
                    "messages", new String[]{
                            "⚠️ Chatbot service is unavailable.",
                            "Please try again later."
                    },
                    "endSession", false
            );
        }
    }

    // =====================================================
    // LOCAL LAMBDA (HTTP)
    // =====================================================
    @SuppressWarnings("unchecked")
    private Map<String, Object> callLocalLambda(Map<String, Object> payload) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(localLambdaUrl, request, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Empty response from local Lambda");
        }

        return response.getBody();
    }

    // =====================================================
    // AWS LAMBDA (SDK)
    // =====================================================
    @SuppressWarnings("unchecked")
    private Map<String, Object> callAwsLambda(Map<String, Object> payload) throws Exception {

        Map<String, Object> lambdaPayload = new HashMap<>();
        lambdaPayload.put("sessionId", payload.get("sessionId"));
        lambdaPayload.put("message", payload.get("message"));

        byte[] jsonBytes = mapper.writeValueAsBytes(lambdaPayload);

        InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .payload(SdkBytes.fromByteArray(jsonBytes))
                .build();

        InvokeResponse response = lambdaClient.invoke(request);

        String raw = response.payload().asUtf8String();
        log.info("RAW AWS LAMBDA RESPONSE = {}", raw);

        Map<String, Object> root = mapper.readValue(raw, Map.class);

        if (root.containsKey("body")) {
            Object bodyObj = root.get("body");
            if (bodyObj instanceof String bodyStr) {
                return mapper.readValue(bodyStr, Map.class);
            }
            if (bodyObj instanceof Map) {
                return (Map<String, Object>) bodyObj;
            }
        }

        return root;
    }
}
