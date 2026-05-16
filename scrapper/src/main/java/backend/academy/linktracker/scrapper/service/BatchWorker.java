package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.properties.WorkerProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
public class BatchWorker<T> implements ApplicationListener<ApplicationReadyEvent> {

    private final BlockingQueue<T> queue;
    private final ThreadPoolTaskExecutor executor;
    private final WorkerProperties properties;
    private final Consumer<List<T>> batchHandler;

    public BatchWorker(ThreadPoolTaskExecutor executor, WorkerProperties properties, Consumer<List<T>> batchHandler) {

        this.queue = new LinkedBlockingQueue<>(properties.getMaximumNumberOfUnprocessedElements());

        this.executor = executor;
        this.properties = properties;
        this.batchHandler = batchHandler;
    }

    public void submit(T item) {

        long start = System.nanoTime();

        try {
            queue.put(item);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Thread {} was interrupted", Thread.currentThread().getName());
            return;
        }

        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        if (tookMs > 10) {
            log.warn("Workers are overloaded, enqueue took {} ms", tookMs);
        }
    }

    protected void start() {

        int workers = executor.getCorePoolSize();

        for (int i = 0; i < workers; i++) {
            executor.submit(this::runWorker);
        }
    }

    private void runWorker() {

        List<T> buffer = new ArrayList<>(properties.getBatchSize());

        while (!Thread.currentThread().isInterrupted()) {

            try {

                T first = queue.poll(properties.getFlushTimeout(), TimeUnit.MILLISECONDS);

                if (first != null) {
                    buffer.add(first);
                }

                queue.drainTo(buffer, properties.getBatchSize() - buffer.size());

                if (!buffer.isEmpty()) {
                    batchHandler.accept(buffer);
                    buffer.clear();
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                log.info("Worker {} interrupted", Thread.currentThread().getName());
            }
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        start();
    }
}
