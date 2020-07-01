package main.controllers;

import main.entity.Action;
import main.service.ActionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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
    public String getMainPage(Model model) {
        return buildIndex(model);
    }

    @PostMapping
    public String putAction(@RequestParam String content, Model model) {
        if (actionService.addActionToDB(new Action(content, new Date().getTime())) != 0) {
            return buildIndex(model);
        }
        model.addAttribute("httpStatus", HttpStatus.SERVICE_UNAVAILABLE);
        return ERROR_PAGE;
    }

    @PostMapping("{id}/del/")
    public String deleteAction(@PathVariable Long id, Model model) {
        if (actionService.deleteActionIfExists(id)) {
            return REDIRECT_TO_MAIN_PAGE;
        }
        model.addAttribute("httpStatus", HttpStatus.NOT_FOUND);
        return ERROR_PAGE;
    }

    @GetMapping("{id}/edit")
    public String editAction(@PathVariable Long id, Model model) {
        return actionService.getAction(id)
                .map(a -> {
                    model.addAttribute("content", a.getContent());
                    model.addAttribute("id", id);
                    return EDIT_PAGE;
                }).orElseGet(() -> {
                    model.addAttribute("httpStatus", HttpStatus.NOT_FOUND);
                    return ERROR_PAGE;
                });
    }

    @PostMapping(EDIT_PAGE)
    public String putEditedAction(@RequestParam Long id, @RequestParam String content, Model model) {
        if (actionService.replaceActionToDBIfExists(new Action(id, content, new Date().getTime())) != 0) {
            return REDIRECT_TO_MAIN_PAGE;
        }
        model.addAttribute("httpStatus", HttpStatus.NOT_FOUND);
        return ERROR_PAGE;
    }

    @GetMapping("filter")
    public String filterActions(@RequestParam String filter, Model model) {
        List<Action> actionList = actionService.getAllActionsByContent(filter);
        if (!actionList.isEmpty()) {
            model.addAttribute("allActions", actionList);
            return MAIN_PAGE;
        }
        model.addAttribute("httpStatus", HttpStatus.NOT_FOUND);
        return ERROR_PAGE;
    }

    private String buildIndex(Model model) {
        model.addAttribute("allActions",
                actionService.getAllActions());
        return MAIN_PAGE;
    }
}
