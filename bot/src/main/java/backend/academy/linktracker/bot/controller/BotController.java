package backend.academy.linktracker.bot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotController {

    @PostMapping
    public void getUpdates() {
    }
}
