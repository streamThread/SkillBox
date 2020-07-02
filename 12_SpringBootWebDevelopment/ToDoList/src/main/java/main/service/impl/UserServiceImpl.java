package main.service.impl;

import main.entity.User;
import main.repos.UserRepository;
import main.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getUserByLogin(String login) {
        return Optional.ofNullable(userRepository.findByLogin(login));
    }

    @Override
    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String login) {
        return userRepository.findByLogin(login);
    }
}
