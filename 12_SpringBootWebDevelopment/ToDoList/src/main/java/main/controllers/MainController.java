package main.controllers;

import main.service.ActionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MainController {

  private static final String ERROR_PAGE = "errorlog";
  private static final String MAIN_PAGE = "index";
  private static final String EDIT_PAGE = "edit";
  private static final String REDIRECT_TO_MAIN_PAGE = "redirect:/";
  private final ActionService actionService;

  public MainController(ActionService actionService) {
    this.actionService = actionService;
  }

  @GetMapping
  public String getMainPage() {
    return "index";
  }
}
