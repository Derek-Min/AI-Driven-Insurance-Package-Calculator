package insurance_package.service;

import insurance_package.mongo.model.ChatSession;
import insurance_package.mongo.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AwsChatbotService {

    private final ChatSessionRepository chatSessionRepository;

    @Value("${chatbot.engine-url}")
    private String chatbotEngineUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> askChatbot(Map<String, Object> payload) {

        String sessionId = String.valueOf(payload.get("sessionId"));
        String message = String.valueOf(payload.get("message"));

        // Save user message
        saveMessage(sessionId, "user", message);

        // 🔥 Call Python Lambda server
        Map<String, Object> response =
                restTemplate.postForObject(
                        chatbotEngineUrl,
                        payload,
                        Map.class
                );

        // Save bot messages
        if (response != null && response.get("messages") instanceof List<?> msgs) {
            msgs.forEach(m ->
                    saveMessage(sessionId, "bot", String.valueOf(m))
            );
        }

        return response;
    }

    private void saveMessage(String sessionId, String sender, String text) {

        ChatSession session = chatSessionRepository
                .findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatSession cs = new ChatSession();
                    cs.setSessionId(sessionId);
                    return cs;
                });

        ChatSession.Message msg = new ChatSession.Message();
        msg.setSender(sender);
        msg.setText(text);

        session.getMessages().add(msg);
        chatSessionRepository.save(session);
    }
}
