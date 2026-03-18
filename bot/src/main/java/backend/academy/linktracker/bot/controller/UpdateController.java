package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.LinkUpdateService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/updates")
public class UpdateController {

    private final LinkUpdateService linkUpdateService;

    public UpdateController(LinkUpdateService linkUpdateService) {
        this.linkUpdateService = linkUpdateService;
    }

    @PostMapping
    public void getUpdates(@RequestBody @Valid LinkUpdate linkUpdate) {
        linkUpdateService.sendUpdateMessage(linkUpdate);
    }
}
