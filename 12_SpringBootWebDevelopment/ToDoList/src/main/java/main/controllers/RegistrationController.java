package main.controllers;

import java.util.Collections;
import main.entity.Role;
import main.entity.User;
import main.entity.dto.AddUserDTO;
import main.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

  UserService userService;

  public RegistrationController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/registration")
  public String registration() {
    return "registration";
  }

  @PostMapping("/registration")
  public String addUser(AddUserDTO addUserDTO, Model model) {
    return userService.getUserByLogin(addUserDTO.getLogin())
        .map(a -> {
          model.addAttribute("message", "User exists!");
          return "registration";
        })
        .orElseGet(() -> {
          User user = new User();
          user.setLogin(addUserDTO.getLogin());
          user.setPassword(addUserDTO.getPassword());
          user.setActive(true);
          user.setRoles(Collections.singleton(Role.USER));
          userService.addUser(user);
          return "redirect:/login";
        });
  }
}
