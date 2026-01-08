package insurance_package.mongo.repository;

import insurance_package.mongo.model.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatSessionRepository
        extends MongoRepository<ChatSession, String> {

    Optional<ChatSession> findBySessionId(String sessionId);
}
