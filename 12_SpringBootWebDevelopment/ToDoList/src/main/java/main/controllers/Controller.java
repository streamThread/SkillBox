package main.controllers;

import main.model.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;

@org.springframework.stereotype.Controller
public class Controller {

    @Autowired
    private ActionRepository actionRepository;

    @RequestMapping("/")
    public String getMainPage(Model model) {
        model.addAttribute("allActions", new ArrayList<>(actionRepository.findAllByOrderById()));
        return "index";
    }
}
