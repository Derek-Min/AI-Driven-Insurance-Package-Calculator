package insurance_package.controller;

import insurance_package.model.PremiumResult;
import insurance_package.model.QuotationRequest;
import insurance_package.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Profile("mongo")
@RestController
@RequestMapping("/api/quote")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatQuoteController {

    private final PricingService pricingService;

    // =========================================================
    // LIFE QUOTATION (FROM CHATBOT)
    // =========================================================
    @PostMapping("/life/from-chat")
    public ResponseEntity<Map<String, Object>> lifeFromChat(
            @RequestBody Map<String, Object> payload
    ) {
        return processChatQuote("Life", payload);
    }

    // =========================================================
    // MOTOR QUOTATION (FROM CHATBOT)
    // =========================================================
    @PostMapping("/motor/from-chat")
    public ResponseEntity<Map<String, Object>> motorFromChat(
            @RequestBody Map<String, Object> payload
    ) {
        return processChatQuote("Motor", payload);
    }

    // =========================================================
    // SHARED LOGIC
    // =========================================================
    private ResponseEntity<Map<String, Object>> processChatQuote(
            String line,
            Map<String, Object> payload
    ) {
        try {
            Map<String, Object> slots = extractSlots(payload);

            String email = String.valueOf(slots.getOrDefault("email", "")).trim();
            if (email.isEmpty()) {
                return error("Email is required");
            }

            QuotationRequest req = new QuotationRequest();
            req.setLine(line);
            req.setCustomerName(
                    String.valueOf(slots.getOrDefault("customer_name", "Valued Customer"))
            );
            req.setEmail(email);
            req.setSlots(slots);

            // 🔥 ONE call does everything
            PremiumResult result = pricingService.calculatePremium(req);

            return ResponseEntity.ok(
                    Map.of(
                            "ok", true,
                            "line", line,
                            "totalPremium", result.getTotalPremium(),
                            "riskScore", result.getRiskScore()
                    )
            );

        } catch (Exception ex) {
            return error(line + " quotation failed: " + ex.getMessage());
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("ok", false, "error", message));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractSlots(Map<String, Object> payload) {
        Object slotsObj = payload.get("slots");
        if (slotsObj instanceof Map<?, ?> map) {
            Map<String, Object> out = new HashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() != null) {
                    out.put(String.valueOf(e.getKey()), e.getValue());
                }
            }
            return out;
        }
        return Collections.emptyMap();
    }
}
