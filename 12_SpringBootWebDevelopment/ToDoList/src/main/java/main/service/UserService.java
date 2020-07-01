package main.service;

import main.entity.User;

import java.util.Optional;

public interface UserService {

    Optional<User> getUserByLogin(String login);

    User addUser(User user);
}
