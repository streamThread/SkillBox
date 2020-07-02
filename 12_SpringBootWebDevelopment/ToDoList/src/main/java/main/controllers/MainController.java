package main.controllers;

import main.entity.Action;
import main.entity.User;
import main.service.ActionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/")
public class MainController {

    private final ActionService actionService;

    private static final String ERROR_PAGE = "errorlog";
    private static final String MAIN_PAGE = "index";
    private static final String EDIT_PAGE = "edit";
    private static final String REDIRECT_TO_MAIN_PAGE = "redirect:/";

    public MainController(ActionService actionService) {
        this.actionService = actionService;
    }

    @GetMapping
    public String getMainPage(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "") String filter,
            Model model) {
        List<Action> actionList;
        if (!filter.isEmpty()) {
            model.addAttribute("filter", filter);
            actionList = actionService.getAllActionsByUserAndContent(user, filter);
        } else {
            actionList = actionService.getAllActionsByUser(user);
        }
        return !actionList.isEmpty() ?
                buildIndexPage(actionList, model) :
                buildErrorPage(HttpStatus.NOT_FOUND, model);
    }


    @PostMapping
    public String putAction(
            @AuthenticationPrincipal User user,
            @RequestParam String content, Model model) {
        Action action = new Action();
        action.setContent(content);
        action.setTimeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        action.setOwner(user);
        return actionService.addActionToDB(action) != 0 ?
                buildIndexPage(actionService.getAllActionsByUser(user), model) :
                buildErrorPage(HttpStatus.SERVICE_UNAVAILABLE, model);
    }

    @PostMapping("{id}/del/")
    public String deleteAction(@PathVariable Long id, Model model) {
        return actionService.deleteActionIfExists(id) ?
                REDIRECT_TO_MAIN_PAGE :
                buildErrorPage(HttpStatus.NOT_FOUND, model);
    }

    @PostMapping("{id}/edit")
    public String editAction(@PathVariable Long id, Model model) {
        return actionService.getAction(id)
                .map(a -> {
                    model.addAttribute("content", a.getContent())
                            .addAttribute("id", id);
                    return EDIT_PAGE;
                }).orElseGet(() -> buildErrorPage(HttpStatus.NOT_FOUND, model));
    }

    @PostMapping(EDIT_PAGE)
    public String putEditedAction(
            @AuthenticationPrincipal User user,
            @RequestParam Long id, @RequestParam String content, Model model) {
        Action action = new Action();
        action.setId(id);
        action.setContent(content);
        action.setTimeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        action.setOwner(user);
        return actionService.replaceActionToDBIfExists(action) != 0 ?
                REDIRECT_TO_MAIN_PAGE :
                buildErrorPage(HttpStatus.NOT_FOUND, model);
    }

    private String buildIndexPage(List<Action> actionList, Model model) {
        model.addAttribute("allActions", actionList);
        return MAIN_PAGE;
    }

    private String buildErrorPage(HttpStatus httpStatus, Model model) {
        model.addAttribute("httpStatus", httpStatus);
        return ERROR_PAGE;
    }
}
