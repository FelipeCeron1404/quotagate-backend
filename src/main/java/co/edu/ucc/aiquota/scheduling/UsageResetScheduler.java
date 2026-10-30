package co.edu.ucc.aiquota.scheduling;

import co.edu.ucc.aiquota.quota.QuotaLedgerService;
import co.edu.ucc.aiquota.ratelimit.RequestWindowTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UsageResetScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsageResetScheduler.class);

    private final RequestWindowTracker requestWindowTracker;
    private final QuotaLedgerService quotaLedgerService;

    public UsageResetScheduler(RequestWindowTracker requestWindowTracker, QuotaLedgerService quotaLedgerService) {
        this.requestWindowTracker = requestWindowTracker;
        this.quotaLedgerService = quotaLedgerService;
    }

    @Scheduled(cron = "0 * * * * *", zone = "${app.time-zone}")
    public void resetRateLimitWindow() {
        requestWindowTracker.resetWindow();
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "${app.time-zone}")
    public void resetMonthlyQuota() {
        int resetAccounts = quotaLedgerService.resetMonthlyUsage();
        LOGGER.info("Monthly token quota reset for {} accounts", resetAccounts);
    }
}
