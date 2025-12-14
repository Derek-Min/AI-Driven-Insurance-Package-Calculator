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
            // ---- Build payload for Lambda ----
            Map<String, Object> lambdaPayload = new HashMap<>();
            lambdaPayload.put("sessionId", payload.get("sessionId"));

            Object text = payload.get("text");
            if (text != null) {
                lambdaPayload.put("inputText", text);
            }

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

            // ---- Parse API Gateway–style envelope ----
            Map<String, Object> envelope = mapper.readValue(raw, Map.class);
            Object body = envelope.get("body");

            // ---- Case 1: body is JSON string ----
            if (body instanceof String) {
                Map<String, Object> parsed =
                        mapper.readValue((String) body, Map.class);

                Map<String, Object> clean = new HashMap<>();
                clean.put("sessionId", parsed.get("sessionId"));
                clean.put("reply", parsed.get("reply"));
                clean.put("shouldEndSession", parsed.get("shouldEndSession"));

                return clean;
            }

            // ---- Case 2: body already a Map ----
            if (body instanceof Map) {
                Map<String, Object> parsed = (Map<String, Object>) body;

                Map<String, Object> clean = new HashMap<>();
                clean.put("sessionId", parsed.get("sessionId"));
                clean.put("reply", parsed.get("reply"));
                clean.put("shouldEndSession", parsed.get("shouldEndSession"));

                return clean;
            }

            // ---- Fallback ----
            return Map.of(
                    "error",
                    "No valid body found in Lambda response",
                    "rawResponse",
                    envelope
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }
}
