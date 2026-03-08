package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.impl.InMemeoryLinkRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LinkUpdateScheduler {

    private final LinkUpdateService linkUpdateService;
    private final InMemeoryLinkRepository inMemeoryLinkRepository;

    public LinkUpdateScheduler(
        LinkUpdateService linkUpdateService,
        InMemeoryLinkRepository inMemeoryLinkRepository
    ) {
        this.linkUpdateService = linkUpdateService;
        this.inMemeoryLinkRepository = inMemeoryLinkRepository;
    }

    @Scheduled(fixedDelay = 60000)
    public void checkLinks() {
        List<Link> links = inMemeoryLinkRepository.findAll();

        for (Link link : links) {
            linkUpdateService.process(link);
        }
    }

}
