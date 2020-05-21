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

@org.springframework.stereotype.Controller
public class MainController {

    @Autowired
    private ToDoListController toDoListController;

    @GetMapping("/")
    public String getMainPage(Model model) {
        model.addAttribute("allActions",
                toDoListController.getAllActions(null, null).getBody());
        model.addAttribute("time", new Date().getTime());
        return "index";
    }

    @PostMapping("/act")
    public String putAction(@RequestParam String content, Model model) {
        HttpStatus httpStatus = toDoListController.putAction(
                content, new Date().getTime()).getStatusCode();
        if (httpStatus.equals(HttpStatus.CREATED)) {
            return "redirect:/";
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
}
