package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
import backend.academy.linktracker.scrapper.repository.impl.InMemoryLinkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class LinkUpdateScheduler {

    private final LinkUpdateService linkUpdateService;
    private final InMemoryLinkRepository inMemoryLinkRepository;

    public LinkUpdateScheduler(
        LinkUpdateService linkUpdateService,
        InMemoryLinkRepository inMemoryLinkRepository
    ) {
        this.linkUpdateService = linkUpdateService;
        this.inMemoryLinkRepository = inMemoryLinkRepository;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.interval-update-ms}")
    public void checkLinks() {
        Set<Link> links = inMemoryLinkRepository.findAll();

        for (Link link : links) {
            try {
                linkUpdateService.process(link);
            } catch (BotApiException ex) {
                log.error("Unhandled BOT API error", ex);
            } catch (ExternalApiException ex) {
                log.error("Unhandled EXTERNAL API error",
                    kv("status", ex.getStatusCode()),
                    kv("url", ex.getUrl()),
                    ex
                );
            } catch (Exception ex) {
                log.error("Error processing link", ex);
            }
        }
    }

}
