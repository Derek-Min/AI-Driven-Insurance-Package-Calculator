package insurance_package.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AwsChatbotService {

    private final LambdaClient lambdaClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${aws.lambda.chatbot.name}")
    private String functionName;

    @SuppressWarnings("unchecked")
    public Map<String, Object> askChatbot(Map<String, Object> payload) {
        try {
            // ---- Build the payload that Lambda expects ----
            Map<String, Object> lambdaPayload = new HashMap<>();

            // session id from frontend
            lambdaPayload.put("sessionId", payload.get("sessionId"));

            // frontend sends "text" -> Lambda expects "inputText"
            Object text = payload.get("text");
            if (text != null) {
                lambdaPayload.put("inputText", text);
            }

            // if frontend sends type = "confirm", map to action=confirm_send
            Object type = payload.get("type");
            if ("confirm".equals(type)) {
                lambdaPayload.put("action", "confirm_send");
            }

            byte[] jsonBytes = mapper.writeValueAsBytes(lambdaPayload);

            InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(SdkBytes.fromByteArray(jsonBytes))
                    .build();

            InvokeResponse response = lambdaClient.invoke(request);
            String raw = response.payload().asUtf8String();

            System.out.println("RAW LAMBDA RESPONSE = " + raw);

            // Lambda normally returns an "API Gateway style" envelope:
            // { statusCode: 200, headers: {...}, body: "{...json...}" }
            Map<String, Object> envelope = mapper.readValue(raw, Map.class);

            Object body = envelope.get("body");

            if (body instanceof String) {
                Map<String, Object> parsed = mapper.readValue((String) body, Map.class);
                System.out.println("PARSED BODY = " + parsed);
                return parsed;
            } else if (body instanceof Map) {
                return (Map<String, Object>) body;
            }

            // In error cases Lambda may return errorMessage without "body"
            return Map.of("error", "No body found in Lambda response: " + envelope);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }
}
