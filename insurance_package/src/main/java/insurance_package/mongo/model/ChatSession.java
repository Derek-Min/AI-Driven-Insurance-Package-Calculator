package insurance_package.mongo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "chat_sessions")
public class ChatSession {

    @Id
    private String id;

    private String sessionId;

    private List<Message> messages = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    public static class Message {
        private String sender;   // user | bot
        private String text;
        private LocalDateTime timestamp = LocalDateTime.now();
    }
}
