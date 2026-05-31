package backend.academy.linktracker.scrapper.service.logs;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class ScrapperMetricsService {

    private final MeterRegistry registry;

    private final ConcurrentMap<String, AtomicInteger> trackedLinks = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Counter> apiRequestCounters = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Counter> apiErrorCounters = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Timer> requestTimers = new ConcurrentHashMap<>();

    public ScrapperMetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    public void setTrackedLinksCount(String domain, int currentCount) {

        AtomicInteger gaugeValue = trackedLinks.computeIfAbsent(domain, this::registerGauge);

        gaugeValue.set(currentCount);
    }

    private AtomicInteger registerGauge(String domain) {

        AtomicInteger value = new AtomicInteger(0);

        Gauge.builder("links_on_track_total", value, AtomicInteger::get)
                .description("Tracked links count")
                .tag("tracked_source", domain)
                .register(registry);

        return value;
    }

    public void incrementApiRequest(String source) {

        apiRequestCounters
                .computeIfAbsent(source, src -> Counter.builder("api_requests_total")
                        .description("API requests")
                        .tag("source", src)
                        .tag("status", "success")
                        .register(registry))
                .increment();
    }

    public void incrementApiError(String source) {

        apiErrorCounters
                .computeIfAbsent(source, src -> Counter.builder("api_requests_total")
                        .description("API requests")
                        .tag("source", src)
                        .tag("status", "error")
                        .register(registry))
                .increment();
    }

    public Timer.Sample startRequestTimer() {
        return Timer.start(registry);
    }

    public void stopRequestTimer(Timer.Sample sample, String scope, String scopeType) {

        String key = scope + ":" + scopeType;

        Timer timer = requestTimers.computeIfAbsent(key, ignored -> Timer.builder("request_duration")
                .description("External request duration")
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
                        Duration.ofSeconds(5),
                        Duration.ofSeconds(10))
                .register(registry));

        sample.stop(timer);
    }

    public <T> T timeExternalCall(String scope, String scopeType, String source, ExternalCall<T> call) {

        Timer.Sample sample = startRequestTimer();

        try {

            T result = call.execute();

            incrementApiRequest(source);

            return result;

        } catch (Exception e) {

            incrementApiError(source);

            throw e;

        } finally {

            stopRequestTimer(sample, scope, scopeType);
        }
    }

    @FunctionalInterface
    public interface ExternalCall<T> {
        T execute();
    }
}
