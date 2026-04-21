package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class LinkUpdateScheduler {

    private final LinkUpdateService linkUpdateService;
    private final LinkService linkService;
    private final ExecutorService executorService;
    private final SchedulerProperties schedulerProperties;

    public LinkUpdateScheduler(
            LinkUpdateService linkUpdateService,
            LinkService linkService,
            @Qualifier("externalApiExecutor") ExecutorService executor,
            SchedulerProperties schedulerProperties) {

        this.executorService = executor;
        this.schedulerProperties = schedulerProperties;
        this.linkUpdateService = linkUpdateService;
        this.linkService = linkService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.interval-update-ms}")
    public void checkLinks() {

        Slice<Link> linksLastBatch;
        long lastId = 0;

        do {

            linksLastBatch = linkService.findLinkBatch(lastId, schedulerProperties.getBatchSize());

            if (linksLastBatch.getContent().isEmpty()) {
                log.warn("No one links found");
                return;
            }

            processMultithreadBatchLinks(linksLastBatch.getContent());
            lastId = linksLastBatch.getContent().getLast().getLinkId();
        } while (linksLastBatch.hasNext());

    }

    @SuppressFBWarnings(
        value = "SLF4J_PLACE_HOLDER_MISMATCH",
        justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void processBatchLinks(List<Link> links) {

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

    private void processMultithreadBatchLinks(List<Link> batchLinks) {

        List<List<Link>> dividedBatch = splitBatchIntoChunks(batchLinks);

        for (List<Link> linksChunk : dividedBatch) {

            executorService.submit(() -> processBatchLinks(linksChunk));
        }
    }

    private List<List<Link>> splitBatchIntoChunks(List<Link> batchLinks) {

        int batchSize = Math.min(batchLinks.size(), 1) ;
        int threadPoolSize = schedulerProperties.getThreadPoolSize();

        if (batchSize < threadPoolSize) return List.of(batchLinks);

        int chunkSize = batchSize / threadPoolSize;
        List<List<Link>> linkChunks = new ArrayList<>();

        for (int i = 0; i < batchSize; i += chunkSize) {
            linkChunks.add(batchLinks.subList(i, Math.min(i + chunkSize, batchSize)));
        }

        return linkChunks;
    }

}
