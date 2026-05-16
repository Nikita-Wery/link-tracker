// package backend.academy.linktracker.scrapper.service;
//
// import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
// import backend.academy.linktracker.scrapper.properties.OutboxEventUpdateWorkerProperties;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.boot.context.event.ApplicationReadyEvent;
// import org.springframework.context.event.EventListener;
// import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
// import org.springframework.stereotype.Component;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.concurrent.BlockingQueue;
// import java.util.concurrent.LinkedBlockingQueue;
// import java.util.concurrent.TimeUnit;
//
// @Component
// @Slf4j
// public class OutboxEventUpdateWorker {
//
//    private final BlockingQueue<OutboxEventUpdateDto> queue;
//    private final ThreadPoolTaskExecutor executor;
//    private final OutboxEventUpdateWorkerProperties properties;
//    private final OutboxEventService outboxEventService;
//
//    public OutboxEventUpdateWorker(
//            @Qualifier("linkUpdateWorkerExecutor") ThreadPoolTaskExecutor executor,
//            OutboxEventUpdateWorkerProperties properties,
//            OutboxEventService outboxEventService) {
//
//        this.queue = new LinkedBlockingQueue<>(properties.getMaximumNumberOfNotUpdatedLinks());
//        this.executor = executor;
//        this.properties = properties;
//        this.outboxEventService = outboxEventService;
//    }
//
//    public void submit(OutboxEventUpdateDto outboxEventUpdateDto) {
//
//        long start = System.nanoTime();
//
//        try {
//            queue.put(outboxEventUpdateDto);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.info("Thread {} was interrupted", Thread.currentThread().getName());
//        }
//
//        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
//
//        if (tookMs > 10) {
//            log.warn(
//                    "LinkUpdateWorkers are overloaded," + " the current time to add tasks is {},"
//                            + " total workers {}"
//                            + " flush timeout {}",
//                    tookMs,
//                    properties.getMaxPoolSize(),
//                    properties.getFlushTimeout());
//        }
//    }
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void start() {
//
//        int workers = executor.getCorePoolSize();
//
//        for (int i = 0; i < workers; i++) {
//            executor.submit(this::runWorker);
//        }
//    }
//
//    public void runWorker() {
//        log.info(
//                "LinkUpdateWorker {} has started its work",
//                Thread.currentThread().getName());
//
//        List<OutboxEventUpdateDto> buffer = new ArrayList<>(properties.getBatchSize());
//
//        while (!Thread.currentThread().isInterrupted()) {
//            try {
//
//                OutboxEventUpdateDto first = queue.poll(properties.getFlushTimeout(), TimeUnit.MILLISECONDS);
//
//                if (first != null) {
//                    buffer.add(first);
//                }
//
//                queue.drainTo(buffer, properties.getBatchSize() - buffer.size());
//
//                if (!buffer.isEmpty()) {
//                    outboxEventService.batchUpdateOutboxEventStatuses(buffer);
//                    buffer.clear();
//                }
//
//            } catch (InterruptedException e) {
//                log.info(
//                        "LinkUpdateWorker: {} interrupted",
//                        Thread.currentThread().getName());
//                Thread.currentThread().interrupt();
//            }
//        }
//    }
// }
