package insurance_package.controller;

import insurance_package.model.PremiumResult;
import insurance_package.model.QuotationRequest;
import insurance_package.model.Quote;
import insurance_package.service.PdfQuotationService;
import insurance_package.service.PricingService;
import lombok.RequiredArgsConstructor;
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

    // -------------------------
    // LIFE
    // -------------------------
    @PostMapping("/life/from-chat")
    public Map<String, Object> lifeFromChat(@RequestBody Map<String, Object> payload) {

        Map<String, Object> slots = extractSlots(payload);

        QuotationRequest req = new QuotationRequest();
        req.setLine("Life");
        req.setCustomerName(
                String.valueOf(slots.getOrDefault("customer_name", "Valued Customer"))
        );
        req.setEmail(String.valueOf(slots.get("email")));
        req.setSlots(slots);

        PremiumResult result = pricingService.calculatePremium(req);

        // =========================
        // BUILD QUOTE (FULL DATA)
        // =========================
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

        // Generate PDF (optional for life)
        pdfQuotationService.generateQuotationPdf(quote);

        return Map.of(
                "ok", true,
                "premium", result.getTotalPremium(),
                "riskScore", result.getRiskScore()
        );
    }

    // -------------------------
    // MOTOR
    // -------------------------
    @PostMapping("/motor/from-chat")
    public Map<String, Object> motorFromChat(@RequestBody Map<String, Object> payload) {

        Map<String, Object> slots = extractSlots(payload);

        System.out.println("MOTOR SLOTS = " + slots);


        QuotationRequest req = new QuotationRequest();
        req.setLine("Motor");
        req.setCustomerName(
                String.valueOf(slots.getOrDefault("customer_name", "Valued Customer"))
        );
        req.setEmail(String.valueOf(slots.get("email")));
        req.setSlots(slots);

        PremiumResult result = pricingService.calculatePremium(req);

        // =========================
        // BUILD QUOTE (FULL DATA)
        // =========================
        Quote quote = Quote.builder()
                .quoteId("Q-" + System.currentTimeMillis())
                .line("Motor")
                .currency(result.getBreakdown().getCurrency())
                .customerName(req.getCustomerName())
                .customerEmail(req.getEmail())
                .requestDetails(slots)
                // 🔑 CRITICAL: full breakdown stored here
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
        System.out.println("SLOTS = " + slots);


        // Generate PDF
        pdfQuotationService.generateQuotationPdf(quote);

        return Map.of(
                "ok", true,
                "premium", result.getTotalPremium(),
                "riskScore", result.getRiskScore()
        );
    }

    // -------------------------
    // helpers
    // -------------------------
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
