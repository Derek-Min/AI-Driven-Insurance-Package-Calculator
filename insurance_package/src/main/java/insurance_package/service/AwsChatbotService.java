package insurance_package.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import insurance_package.model.QuotationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AwsChatbotService {

    private final LambdaClient lambdaClient;
    private final PricingService pricingService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${aws.lambda.chatbot.name}")
    private String functionName;

    @SuppressWarnings("unchecked")
    public Map<String, Object> askChatbot(Map<String, Object> payload) {

        try {
            // -----------------------------
            // 1) Build payload for Lambda
            // -----------------------------
            Map<String, Object> lambdaPayload = new HashMap<>();
            lambdaPayload.put("sessionId", payload.get("sessionId"));

            Object message = payload.get("message");
            if (message != null) {
                // ✅ support both modes (your lambda uses inputText)
                lambdaPayload.put("inputText", String.valueOf(message));
                lambdaPayload.put("message", String.valueOf(message));
            }

            if ("confirm".equalsIgnoreCase(String.valueOf(message))) {
                lambdaPayload.put("action", "confirm_send");
            }

            byte[] jsonBytes = mapper.writeValueAsBytes(lambdaPayload);

            InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(SdkBytes.fromByteArray(jsonBytes))
                    .build();

            // -----------------------------
            // 2) Invoke Lambda
            // -----------------------------
            InvokeResponse response = lambdaClient.invoke(request);
            String raw = response.payload().asUtf8String();

            System.out.println("RAW LAMBDA RESPONSE = " + raw);

            // -----------------------------
            // 3) Parse response (robust)
            // -----------------------------
            Map<String, Object> root = mapper.readValue(raw, Map.class);

            Map<String, Object> parsed = null;

            // Case A: API Gateway envelope: { statusCode, headers, body }
            if (root.containsKey("body")) {
                Object bodyObj = root.get("body");

                if (bodyObj instanceof String bodyStr) {
                    // body is JSON string
                    parsed = mapper.readValue(bodyStr, Map.class);
                } else if (bodyObj instanceof Map) {
                    // body already map
                    parsed = (Map<String, Object>) bodyObj;
                }
            }

            // Case B: Direct JSON response (no envelope)
            if (parsed == null && root.containsKey("reply")) {
                parsed = root;
            }

            // Still invalid
            if (parsed == null) {
                return Map.of(
                        "reply", "⚠️ Invalid response from chatbot service.",
                        "shouldEndSession", false
                );
            }

            // -----------------------------
            // 4) Normalize output to frontend
            // -----------------------------
            parsed.putIfAbsent("sessionId", payload.get("sessionId"));
            return parsed;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "reply", "❗ Internal chatbot error. Please try again.",
                    "shouldEndSession", false
            );
        }
    }


    /**
     * ✅ Integration-only method.
     * - Does NOT modify any algorithms.
     * - Uses RuleEngines exactly as they are.
     * - Sends "quotation letter" using existing SES HTML email (no PDF param).
     */
    @SuppressWarnings("unchecked")
    public void processQuotation(Map<String, Object> extra) {

        try {
            if (extra == null) {
                System.out.println("⚠️ processQuotation called with null data");
                return;
            }

            String intent = String.valueOf(extra.get("intent"));
            Map<String, Object> slots =
                    (Map<String, Object>) extra.get("slots");

            if (slots == null) {
                System.out.println("⚠️ Missing slots from chatbot");
                return;
            }

            // -----------------------------
            // Build QuotationRequest (ONLY THIS)
            // -----------------------------
            QuotationRequest req = new QuotationRequest();
            req.setLine(
                    intent.equalsIgnoreCase("LIFE") ? "Life" : "Motor"
            );
            req.setSlots(slots);

            // -----------------------------
            // Delegate EVERYTHING else
            // -----------------------------
            pricingService.calculatePremium(req);

            // PDF + MongoDB already handled inside PricingService
            System.out.println("✅ PricingService completed quotation flow.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
