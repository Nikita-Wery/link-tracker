package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.UpdateLinkDto;
import backend.academy.linktracker.scrapper.properties.LinkUpdateWorkerProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LinkUpdateWorker {

    private final BlockingQueue<UpdateLinkDto> queue;
    private final LinkService linkService;
    private final ThreadPoolTaskExecutor executor;
    private final LinkUpdateWorkerProperties properties;

    public LinkUpdateWorker(
            LinkService linkService,
            @Qualifier("linkUpdateWorkerExecutor") ThreadPoolTaskExecutor executor,
            LinkUpdateWorkerProperties properties) {

        this.queue = new LinkedBlockingQueue<>(properties.getMaximumNumberOfNotUpdatedLinks());
        this.executor = executor;
        this.properties = properties;
        this.linkService = linkService;
    }

    public void submit(UpdateLinkDto item) {
        long start = System.nanoTime();

        try {
            queue.put(item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Thread {} was interrupted", Thread.currentThread().getName());
        }

        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        if (tookMs > 10) {
            log.warn(
                    "LinkUpdateWorkers are overloaded," + " the current time to add tasks is {},"
                            + " total workers {}"
                            + " flush timeout {}",
                    tookMs,
                    properties.getMaxPoolSize(),
                    properties.getFlushTimeout());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {

        int workers = executor.getCorePoolSize();

        for (int i = 0; i < workers; i++) {
            executor.submit(this::runWorker);
        }
    }

    public void runWorker() {
        log.info(
                "LinkUpdateWorker {} has started its work",
                Thread.currentThread().getName());

        List<UpdateLinkDto> buffer = new ArrayList<>(properties.getBatchSize());

        while (!Thread.currentThread().isInterrupted()) {
            try {

                UpdateLinkDto first = queue.poll(properties.getFlushTimeout(), TimeUnit.MILLISECONDS);

                if (first != null) {
                    buffer.add(first);
                }

                queue.drainTo(buffer, properties.getBatchSize() - buffer.size());

                if (!buffer.isEmpty()) {
                    linkService.updateLastUpdateBatch(buffer, properties.getBatchSize());
                    buffer.clear();
                }

            } catch (InterruptedException e) {
                log.info(
                        "LinkUpdateWorker: {} interrupted",
                        Thread.currentThread().getName());
                Thread.currentThread().interrupt();
            }
        }
    }
}
