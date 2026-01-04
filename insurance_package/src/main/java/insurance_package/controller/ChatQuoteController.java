package insurance_package.controller;

import insurance_package.model.PremiumResult;
import insurance_package.model.QuotationRequest;
import insurance_package.model.Quote;
import insurance_package.service.PdfQuotationService;
import insurance_package.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/quote")
@RequiredArgsConstructor
public class ChatQuoteController {

    private final PricingService pricingService;
    private final PdfQuotationService pdfQuotationService;

    // =========================================================
    // LIFE QUOTATION (FROM CHATBOT / LAMBDA)
    // =========================================================
    @PostMapping("/life/from-chat")
    public ResponseEntity<Map<String, Object>> lifeFromChat(
            @RequestBody Map<String, Object> payload
    ) {
        try {
            Map<String, Object> slots = extractSlots(payload);

            String email = String.valueOf(slots.getOrDefault("email", "")).trim();
            if (email.isEmpty()) {
                return error("Email is required");
            }

            QuotationRequest req = new QuotationRequest();
            req.setLine("Life");
            req.setCustomerName(
                    String.valueOf(slots.getOrDefault("customer_name", "Valued Customer"))
            );
            req.setEmail(email);
            req.setSlots(slots);

            PremiumResult result = pricingService.calculatePremium(req);

            Quote quote = Quote.builder()
                    .quoteId("Q-" + System.currentTimeMillis())
                    .line("Life")
                    .currency(result.getBreakdown().getCurrency())
                    .customerName(req.getCustomerName())
                    .customerEmail(req.getEmail())
                    .requestDetails(slots)
                    .premiumBreakdown(
                            Map.of(
                                    "items", result.getBreakdown().getItems(),
                                    "totalPremium", result.getTotalPremium(),
                                    "riskScore", result.getRiskScore(),
                                    "currency", result.getBreakdown().getCurrency()
                            )
                    )
                    .totalPremium(result.getTotalPremium())
                    .riskScore(result.getRiskScore())
                    .createdAt(Instant.now())
                    .build();

            // Generate PDF + Send Email (inside service)
            pdfQuotationService.generateQuotationPdf(quote);

            return success(quote);

        } catch (Exception ex) {
            return error("Life quotation failed: " + ex.getMessage());
        }
    }

    // =========================================================
    // MOTOR QUOTATION (FROM CHATBOT / LAMBDA)
    // =========================================================
    @PostMapping("/motor/from-chat")
    public ResponseEntity<Map<String, Object>> motorFromChat(
            @RequestBody Map<String, Object> payload
    ) {
        try {
            Map<String, Object> slots = extractSlots(payload);

            String email = String.valueOf(slots.getOrDefault("email", "")).trim();
            if (email.isEmpty()) {
                return error("Email is required");
            }

            QuotationRequest req = new QuotationRequest();
            req.setLine("Motor");
            req.setCustomerName(
                    String.valueOf(slots.getOrDefault("customer_name", "Valued Customer"))
            );
            req.setEmail(email);
            req.setSlots(slots);

            PremiumResult result = pricingService.calculatePremium(req);

            Quote quote = Quote.builder()
                    .quoteId("Q-" + System.currentTimeMillis())
                    .line("Motor")
                    .currency(result.getBreakdown().getCurrency())
                    .customerName(req.getCustomerName())
                    .customerEmail(req.getEmail())
                    .requestDetails(slots)
                    .premiumBreakdown(
                            Map.of(
                                    "items", result.getBreakdown().getItems(),
                                    "totalPremium", result.getTotalPremium(),
                                    "riskScore", result.getRiskScore(),
                                    "currency", result.getBreakdown().getCurrency()
                            )
                    )
                    .totalPremium(result.getTotalPremium())
                    .riskScore(result.getRiskScore())
                    .createdAt(Instant.now())
                    .build();

            // Generate PDF + Send Email (inside service)
            pdfQuotationService.generateQuotationPdf(quote);

            return success(quote);

        } catch (Exception ex) {
            return error("Motor quotation failed: " + ex.getMessage());
        }
    }

    // =========================================================
    // RESPONSE HELPERS
    // =========================================================
    private ResponseEntity<Map<String, Object>> success(Quote quote) {
        return ResponseEntity.ok(
                Map.of(
                        "ok", true,
                        "quoteId", quote.getQuoteId(),
                        "line", quote.getLine(),
                        "premium", quote.getTotalPremium(),
                        "currency", quote.getCurrency(),
                        "riskScore", quote.getRiskScore()
                )
        );
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "ok", false,
                        "error", message
                )
        );
    }

    // =========================================================
    // SLOT EXTRACTION (SAFE & FLEXIBLE)
    // =========================================================
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
