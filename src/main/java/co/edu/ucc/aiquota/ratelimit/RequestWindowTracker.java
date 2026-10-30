package co.edu.ucc.aiquota.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

@Component
public class RequestWindowTracker {

    private final Map<String, Integer> requestsByConsumer = new ConcurrentHashMap<>();
    private final Clock clock;
    private volatile Instant windowEndsAt;

    public RequestWindowTracker(Clock clock) {
        this.clock = clock;
        this.windowEndsAt = nextMinuteBoundary();
    }

    public boolean tryRegisterRequest(String consumerId, int requestsPerMinute) {
        AtomicBoolean granted = new AtomicBoolean(false);
        requestsByConsumer.compute(consumerId, (id, currentCount) -> {
            int count = currentCount == null ? 0 : currentCount;
            if (count >= requestsPerMinute) {
                return count;
            }
            granted.set(true);
            return count + 1;
        });
        return granted.get();
    }

    public void registerUnlimitedRequest(String consumerId) {
        requestsByConsumer.merge(consumerId, 1, Integer::sum);
    }

    public int requestsInCurrentWindow(String consumerId) {
        return requestsByConsumer.getOrDefault(consumerId, 0);
    }

    public Instant windowEndsAt() {
        return windowEndsAt;
    }

    public long secondsUntilReset() {
        long millis = Duration.between(Instant.now(clock), windowEndsAt).toMillis();
        return Math.max(1L, (millis + 999L) / 1000L);
    }

    public void resetWindow() {
        requestsByConsumer.clear();
        windowEndsAt = nextMinuteBoundary();
    }

    private Instant nextMinuteBoundary() {
        return Instant.now(clock).truncatedTo(ChronoUnit.MINUTES).plus(1, ChronoUnit.MINUTES);
    }
}
