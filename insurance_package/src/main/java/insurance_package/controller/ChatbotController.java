package insurance_package.controller;

import insurance_package.service.AwsChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatbotController {

    private final AwsChatbotService chatbotService;

    // Very simple mapping: POST /chatbot
    @PostMapping("/chatbot")
    public Map<String, Object> ask(@RequestBody Map<String, Object> body) {
        return chatbotService.askChatbot(body);
    }
}
