package main.service;

import java.util.List;
import java.util.Optional;
import main.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

  Optional<User> getUserByLogin(String login);

  User addUser(User user);

  List<User> getAllUsers();
}
