package insurance_package.controller;

import insurance_package.model.User;
import insurance_package.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<User> all() {
        return userRepository.findAll();
    }

    @PostMapping
    public User add(@RequestBody User user) {
        return userRepository.save(user);
    }
}

