package insurance_package.controller;

import insurance_package.service.AwsChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final AwsChatbotService chatbotService;

    @PostMapping("/chatbot")
    public Map<String, Object> ask(@RequestBody Map<String, Object> body) {

        // ✅ Lambda response (already correct)
        Map<String, Object> chatbotReply = chatbotService.askChatbot(body);

        // ✅ FIX: use endSession (NOT shouldEndSession)
        Boolean endSession = (Boolean) chatbotReply.get("endSession");

        if (Boolean.TRUE.equals(endSession)) {

            // Optional: only if extra exists
            Object extraObj = chatbotReply.get("extra");
            if (extraObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> extra = (Map<String, Object>) extraObj;
                chatbotService.processQuotation(extra);
            }
        }

        // ✅ Return Lambda response AS-IS
        return chatbotReply;
    }
}
