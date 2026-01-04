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
        // Simply forward request to chatbot service
        // Lambda handles flow + quotation triggering
        return chatbotService.askChatbot(body);
    }
}
