package main.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Random;

@RestController
public class Controller {

    @GetMapping("/")
    public String getMainPage() {
        boolean choice = new Random().nextBoolean();
        if (choice) {
            return String.valueOf(new Random().nextInt(1000));
        }
        return new Date().toString();
    }
}
