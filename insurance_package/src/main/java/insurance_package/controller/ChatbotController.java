package insurance_package.controller;

import insurance_package.service.AwsChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final AwsChatbotService chatbotService;

    @PostMapping
    public Map<String, Object> chatbot(@RequestBody Map<String, Object> payload) {
        return chatbotService.askChatbot(payload);
    }
}
