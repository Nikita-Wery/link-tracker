package backend.academy.linktracker.bot.logging.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class BotMetricsService {

    private final MeterRegistry registry;

    private final ConcurrentMap<String, Counter> commandCounters = new ConcurrentHashMap<>();

    private final Counter sentNotificationsCounter;

    private final Counter botTotalRequests;

    private final ConcurrentMap<String, Timer> commandTimers = new ConcurrentHashMap<>();

    public BotMetricsService(MeterRegistry registry) {
        this.registry = registry;

        this.botTotalRequests = Counter.builder("bot_requests_total")
                .description("Total number of requests")
                .register(registry);

        this.sentNotificationsCounter = Counter.builder("sent_notification_total")
                .description("Total sent notifications")
                .register(registry);
    }

    public void incrementCommand(String command) {

        commandCounters
                .computeIfAbsent(command, cmd -> Counter.builder("command_requests_total")
                        .description("Processed bot commands")
                        .tag("command", cmd)
                        .register(registry))
                .increment();
    }

    public void incrementSentNotifications() {
        sentNotificationsCounter.increment();
    }

    public void incrementBotTotalRequests() {
        botTotalRequests.increment();
    }

    public Timer.Sample startCommandTimer() {
        return Timer.start(registry);
    }

    public void stopCommandTimer(Timer.Sample sample, String scope, String scopeType) {

        String key = scope + ":" + scopeType;

        Timer timer = commandTimers.computeIfAbsent(key, ignored -> Timer.builder("command_duration")
                .description("Bot command execution duration")
                .tag("scope", scope)
                .tag("scope_type", scopeType)
                .publishPercentileHistogram()
                .serviceLevelObjectives(
                        Duration.ofMillis(10),
                        Duration.ofMillis(50),
                        Duration.ofMillis(100),
                        Duration.ofMillis(250),
                        Duration.ofMillis(500),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(5))
                .register(registry));

        sample.stop(timer);
    }
}
