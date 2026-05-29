package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.client.KafkaBotClient;
import backend.academy.linktracker.ai.model.LinkUpdatesGroup;
import backend.academy.linktracker.ai.model.Priority;
import backend.academy.linktracker.ai.model.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.properties.GroupingProperties;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GropingService {

    @Qualifier("linkUpdateGrouperScheduler")
    private final TaskScheduler scheduler;

    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    private final Map<Long, LinkUpdatesGroup> buffers = new ConcurrentHashMap<>();

    private final GroupingProperties properties;
    private final KafkaBotClient kafkaBotClient;

    public void addProcessedLinkUpdate(long eventKey, ProcessedLinkUpdate processedLinkUpdate) {

        buffers.computeIfAbsent(processedLinkUpdate.id(), id -> {
                    scheduleFlush(id);

                    return new LinkUpdatesGroup(eventKey, new ConcurrentLinkedQueue<>());
                })
                .processedLinkUpdates()
                .add(processedLinkUpdate);
    }

    private void scheduleFlush(long id) {
        if (shuttingDown.get()) {
            return;
        }

        scheduler.schedule(() -> flush(id), Instant.now().plusMillis(properties.getWindowMs()));
    }

    private void flush(long id) {

        log.info("Flushing group of linkUpdsates");

        LinkUpdatesGroup group = buffers.remove(id);

        if (group == null || group.processedLinkUpdates().isEmpty()) {
            return;
        }

        List<ProcessedLinkUpdate> updates = new ArrayList<>(group.processedLinkUpdates());

        ProcessedLinkUpdate groupedUpdate = group(updates);

        kafkaBotClient.send(group.key(), groupedUpdate);
    }

    public ProcessedLinkUpdate group(List<ProcessedLinkUpdate> updates) {

        if (updates.size() == 1) {
            return updates.getFirst();
        }

        if (updates.isEmpty()) {
            throw new IllegalArgumentException("updates is empty");
        }

        ProcessedLinkUpdate first = updates.getFirst();

        Priority highestPriority = first.priority();

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < updates.size(); i++) {

            ProcessedLinkUpdate update = updates.get(i);

            builder.append(i + 1).append(". ").append(update.description()).append('\n');

            if (update.priority().getWeight() > highestPriority.getWeight()) {

                highestPriority = update.priority();
            }
        }

        return new ProcessedLinkUpdate(first.id(), first.url(), builder.toString(), first.tgChatIds(), highestPriority);
    }

    @PreDestroy
    public void shutdown() {

        shuttingDown.set(true);

        log.info("Shutdown detected, flushing remaining groups");

        for (Long id : new ArrayList<>(buffers.keySet())) {
            try {
                flush(id);
            } catch (Exception e) {
                log.error("Failed to flush group id={}", id, e);
            }
        }
    }
}
