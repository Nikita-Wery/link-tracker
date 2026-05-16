package backend.academy.linktracker.bot.controller.rest;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.LinkUpdateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
public class RestUpdateController {

    private final LinkUpdateService linkUpdateService;

    public RestUpdateController(LinkUpdateService linkUpdateService) {
        this.linkUpdateService = linkUpdateService;
    }

    @PostMapping
    public void getUpdates(@RequestBody @Valid LinkUpdate linkUpdate) {
        linkUpdateService.sendUpdateMessage(linkUpdate);
    }
}
