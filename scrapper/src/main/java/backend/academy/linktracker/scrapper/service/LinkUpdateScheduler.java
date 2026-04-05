package backend.academy.linktracker.scrapper.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkUpdateScheduler {

    private final LinkUpdateService linkUpdateService;
    private final LinkRepository linkRepository;

    public LinkUpdateScheduler(LinkUpdateService linkUpdateService, LinkRepository linkRepository) {
        this.linkUpdateService = linkUpdateService;
        this.linkRepository = linkRepository;
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @Scheduled(fixedDelayString = "${app.scheduler.interval-update-ms}")
    public void checkLinks() {
        Set<Link> links = linkRepository.findAll();

        for (Link link : links) {
            try {
                linkUpdateService.process(link);
            } catch (BotApiException ex) {
                log.error("Unhandled BOT API error", ex);
            } catch (ExternalApiException ex) {
                log.error("Unhandled EXTERNAL API error", kv("status", ex.getStatusCode()), kv("url", ex.getUrl()), ex);
            } catch (Exception ex) {
                log.error("Error processing link", ex);
            }
        }
    }
}
