package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.UpdateLinkDto;
import backend.academy.linktracker.scrapper.properties.LinkUpdateWorkerProperties;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LinkUpdateWorker {

    private final BlockingQueue<UpdateLinkDto> queue = new LinkedBlockingQueue<>();
    private final LinkService linkService;
    private final ThreadPoolTaskExecutor executor;

    private final int BATCH_SIZE;
    private final long FLUSH_TIMEOUT_MS;

    public LinkUpdateWorker(LinkRepository linkRepository,
                            LinkUpdateWorkerProperties properties,
                            ThreadPoolTaskExecutor executor,
                            LinkService linkService) {
        this.executor = executor;
        this.BATCH_SIZE = properties.getBatchSize();
        this.FLUSH_TIMEOUT_MS = properties.getFlushTimeout();
        this.linkService = linkService;
    }

    public void submit(UpdateLinkDto item) {
        queue.offer(item);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {

        int workers = executor.getCorePoolSize();

        for (int i = 0; i < workers; i++) {
            executor.submit(this::runWorker);
        }
    }

    public void runWorker() {

        List<UpdateLinkDto> buffer = new ArrayList<>(BATCH_SIZE);

        while (!Thread.currentThread().isInterrupted() && !queue.isEmpty()) {
            try {

                UpdateLinkDto first = queue.poll(FLUSH_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                if (first != null) {
                    buffer.add(first);
                }

                queue.drainTo(buffer, BATCH_SIZE - buffer.size());

                if (!buffer.isEmpty()) {
                    linkService.updateLastUpdateBatch(buffer, BATCH_SIZE);
                    buffer.clear();
                }

            } catch (InterruptedException e) {
                log.info("LinkUpdateWorker: " + Thread.currentThread().getName(), " interrupted.");
                Thread.currentThread().interrupt();
            }
        }
    }

}

