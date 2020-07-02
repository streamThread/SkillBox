package main.service;

import main.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    Optional<User> getUserByLogin(String login);

    User addUser(User user);
}
