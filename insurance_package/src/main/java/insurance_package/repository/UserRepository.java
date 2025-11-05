package insurance_package.repository;

import insurance_package.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    default User findByFullName(String fullName) {
        return null;
    }
}


