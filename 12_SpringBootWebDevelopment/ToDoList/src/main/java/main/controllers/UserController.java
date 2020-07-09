package main.controllers;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import main.entity.Role;
import main.entity.User;
import main.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {

  UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public String userList(Model model) {
    model.addAttribute("users", userService.getAllUsers());
    return "userlist";
  }

  @GetMapping("/{user}")
  public String userEditForm(@PathVariable User user, Model model) {
    model.addAttribute("user", user);
    model.addAttribute("roles", Role.values());
    return "useredit";
  }

  @PostMapping
  public String userSave(
      @RequestParam String userName,
      @RequestParam String userPass,
      @RequestParam Map<String, String> form,
      @RequestParam("userId") User user
  ) {
    user.setLogin(userName);
    user.setPassword(userPass);

    Set<String> roles = Arrays.stream(Role.values())
        .map(Role::name)
        .collect(Collectors.toSet());

    user.getRoles().clear();
    user.setActive(false);

    for (String str : form.keySet()) {
      if (roles.contains(str)) {
        user.getRoles().add(Role.valueOf(str));
      }
      if ("isActive".equals(str)) {
        user.setActive(true);
      }
    }

    userService.addUser(user);
    return "redirect:/user";
  }
}
