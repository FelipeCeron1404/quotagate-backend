package co.edu.ucc.aiquota.quota;

import co.edu.ucc.aiquota.consumer.ConsumerAccount;
import co.edu.ucc.aiquota.consumer.ConsumerAccountService;
import co.edu.ucc.aiquota.generation.GeneratedTextCatalog;
import co.edu.ucc.aiquota.plan.SubscriptionPlan;
import co.edu.ucc.aiquota.ratelimit.RequestWindowTracker;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class QuotaStatusService {

    private final ConsumerAccountService consumerAccountService;
    private final RequestWindowTracker requestWindowTracker;
    private final GeneratedTextCatalog generatedTextCatalog;
    private final Clock clock;

    public QuotaStatusService(ConsumerAccountService consumerAccountService,
                              RequestWindowTracker requestWindowTracker,
                              GeneratedTextCatalog generatedTextCatalog,
                              Clock clock) {
        this.consumerAccountService = consumerAccountService;
        this.requestWindowTracker = requestWindowTracker;
        this.generatedTextCatalog = generatedTextCatalog;
        this.clock = clock;
    }

    public QuotaStatusResponse statusOf(String consumerId) {
        ConsumerAccount account = consumerAccountService.findAccount(consumerId);
        SubscriptionPlan plan = account.getSubscriptionPlan();
        boolean unlimited = plan.isUnlimited();
        return new QuotaStatusResponse(
                account.getId(),
                account.getDisplayName(),
                plan,
                unlimited,
                account.getTokensUsedThisMonth(),
                unlimited ? null : account.remainingTokens(),
                unlimited ? null : plan.monthlyTokenLimit(),
                nextMonthlyResetDate(),
                requestWindowTracker.requestsInCurrentWindow(consumerId),
                unlimited ? null : plan.requestsPerMinute(),
                requestWindowTracker.windowEndsAt(),
                requestWindowTracker.secondsUntilReset(),
                generatedTextCatalog.averageCompletionTokens()
        );
    }

    private LocalDate nextMonthlyResetDate() {
        return LocalDate.now(clock).withDayOfMonth(1).plusMonths(1);
    }
}
