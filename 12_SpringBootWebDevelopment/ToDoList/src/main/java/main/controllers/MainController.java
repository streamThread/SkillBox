package main.controllers;

import main.model.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@org.springframework.stereotype.Controller
public class MainController {

    @Autowired
    private ToDoListController toDoListController;

    @GetMapping("/")
    public String getMainPage(Model model) {
        return buildIndex(model);
    }

    @PostMapping("/")
    public String putAction(@RequestParam String content, Model model) {
        HttpStatus httpStatus = toDoListController.putAction(
                content, new Date().getTime()).getStatusCode();
        if (httpStatus.equals(HttpStatus.CREATED)) {
            return buildIndex(model);
        }
        model.addAttribute("httpStatus", httpStatus.toString());
        return "error";
    }

    @GetMapping("/del/{id}")
    public String deleteAction(@PathVariable Integer id, Model model) {
        HttpStatus httpStatus = toDoListController.deleteAction(id).getStatusCode();
        if (httpStatus.equals(HttpStatus.OK)) {
            return "redirect:/";
        }
        model.addAttribute("httpStatus", httpStatus.toString());
        return "error";
    }

    @GetMapping("/edit/{id}")
    public String editAction(@PathVariable Integer id, Model model) {
        ResponseEntity<Action> actionResponseEntity = toDoListController.getAction(id);
        HttpStatus httpStatus = actionResponseEntity.getStatusCode();
        if (httpStatus.equals(HttpStatus.OK)) {
            model.addAttribute("content", actionResponseEntity.getBody().getContent());
            model.addAttribute("id", id);
            return "edit";
        }
        model.addAttribute("httpStatus", httpStatus);
        return "error";
    }

    @PostMapping("/act-edit")
    public String putEditedAction(@RequestParam Integer id, @RequestParam String content, Model model) {
        HttpStatus httpStatus = toDoListController.replaceAction(id,
                content, new Date().getTime()).getStatusCode();
        if (httpStatus.equals(HttpStatus.CREATED)) {
            return "redirect:/";
        }
        model.addAttribute("httpStatus", httpStatus.toString());
        return "error";
    }

    @PostMapping("/filter")
    public String filterActions(@RequestParam String filter, Model model) {
        ResponseEntity<List<Action>> responseEntity = toDoListController.getAction(
                filter, null, null);
        HttpStatus httpStatus = responseEntity.getStatusCode();
        if (httpStatus.equals(HttpStatus.OK)) {
            model.addAttribute("allActions", responseEntity.getBody());
            return "index";
        }
        model.addAttribute("httpStatus", httpStatus.toString());
        return "error";
    }

    private String buildIndex(Model model) {
        model.addAttribute("allActions",
                toDoListController.getAllActions(null, null).getBody());
        return "index";
    }
}
