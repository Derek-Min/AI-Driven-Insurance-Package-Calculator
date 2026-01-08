package insurance_package.service;

import insurance_package.mongo.model.ChatSession;
import insurance_package.mongo.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import insurance_package.service.PdfQuotationService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AwsChatbotService {

    private final ChatSessionRepository chatSessionRepository;
    private final PdfQuotationService pdfQuotationService;


    public Map<String, Object> askChatbot(Map<String, Object> payload) {

        String sessionId = String.valueOf(payload.get("sessionId"));
        String message = String.valueOf(payload.get("message")).toLowerCase().trim();

        // Save user message
        saveMessage(sessionId, "user", message);

        Map<String, Object> response = new HashMap<>();
        response.put("endSession", false);

        List<String> botMessages;

        // Chatbot logic
        if (message.matches("hi|hello|hey")) {
            botMessages = List.of(
                    "👋 Hello! Welcome to Trust Insurance.",
                    "Do you want Life Insurance or Motor Insurance?"
            );
        }
        else if (message.contains("life")) {
            botMessages = List.of(
                    "🧑‍⚕️ Life Insurance selected.",
                    "Please tell me your age."
            );
        }
        else if (message.contains("motor")) {
            botMessages = List.of(
                    "🚗 Motor Insurance selected.",
                    "Please tell me your car model."
            );
        }
        else if (message.matches("\\d{1,2}")) {
            botMessages = List.of(
                    "📊 Thank you.",
                    "Your estimated premium starts from RM120/month."
            );
            response.put("endSession", true);
        }
        else {
            botMessages = List.of(
                    "❓ I didn't understand that.",
                    "Please type 'life' or 'motor'."
            );
        }

        // Save bot messages
        botMessages.forEach(m -> saveMessage(sessionId, "bot", m));

        response.put("messages", botMessages);
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
