package backend.academy.linktracker.scrapper.service.schedulers;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
import backend.academy.linktracker.scrapper.properties.ApiWorkersProperties;
import backend.academy.linktracker.scrapper.properties.LinkUpdateSchedulerProperties;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkUpdateScheduler {

    private final LinkUpdateService linkUpdateService;
    private final LinkService linkService;
    private final ThreadPoolTaskExecutor executor;
    private final LinkUpdateSchedulerProperties schedulerProperties;
    private final ApiWorkersProperties apiWorkersProperties;

    public LinkUpdateScheduler(
            LinkUpdateService linkUpdateService,
            LinkService linkService,
            @Qualifier("externalApiExecutor") ThreadPoolTaskExecutor executor,
            LinkUpdateSchedulerProperties schedulerProperties,
            ApiWorkersProperties apiWorkerProperties) {

        this.executor = executor;
        this.apiWorkersProperties = apiWorkerProperties;
        this.schedulerProperties = schedulerProperties;
        this.linkUpdateService = linkUpdateService;
        this.linkService = linkService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.link-update.interval-update-ms}")
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
        log.info("ApiWorker {} has started its work", Thread.currentThread().getName());

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

            executor.submit(() -> processBatchLinks(linksChunk));
        }
    }

    private List<List<Link>> splitBatchIntoChunks(List<Link> batchLinks) {

        int batchSize = Math.max(batchLinks.size(), 1);
        int threadPoolSize = apiWorkersProperties.getThreadPoolSize();

        if (batchSize < threadPoolSize) return List.of(batchLinks);

        int chunkSize = batchSize / threadPoolSize;
        List<List<Link>> linkChunks = new ArrayList<>();

        for (int i = 0; i < batchSize; i += chunkSize) {
            linkChunks.add(batchLinks.subList(i, Math.min(i + chunkSize, batchSize)));
        }

        return linkChunks;
    }
}
