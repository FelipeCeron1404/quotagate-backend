package co.edu.ucc.aiquota.usage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(
        name = "daily_usage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"consumer_id", "usage_date"})
)
public class DailyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_id", nullable = false, length = 40)
    private String consumerId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "tokens_consumed", nullable = false)
    private long tokensConsumed;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    protected DailyUsage() {
    }

    public DailyUsage(String consumerId, LocalDate usageDate) {
        this.consumerId = consumerId;
        this.usageDate = usageDate;
        this.tokensConsumed = 0L;
        this.requestCount = 0;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }

    public long getTokensConsumed() {
        return tokensConsumed;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void registerRequest(long tokens) {
        tokensConsumed += tokens;
        requestCount += 1;
    }
}
