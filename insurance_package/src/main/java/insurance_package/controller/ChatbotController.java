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

    // Frontend calls THIS endpoint
    @PostMapping("/chatbot")
    public Map<String, Object> ask(@RequestBody Map<String, Object> body) {

        Map<String, Object> chatbotReply = chatbotService.askChatbot(body);

        Boolean endSession = (Boolean) chatbotReply.get("shouldEndSession");

        if (Boolean.TRUE.equals(endSession)) {
            // 🔑 get intent + slots from Lambda extra
            Map<String, Object> extra =
                    (Map<String, Object>) chatbotReply.get("extra");

            chatbotService.processQuotation(extra);
        }

        return chatbotReply;
    }

}
