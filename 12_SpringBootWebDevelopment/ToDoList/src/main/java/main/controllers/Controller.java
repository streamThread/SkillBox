package main.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Random;

@RestController
public class Controller {

    @GetMapping("/")
    public String getMainPage() {
        return new Random().nextBoolean() ?
                String.valueOf(new Random().nextInt(1000)) :
                new Date().toString();
    }
}
