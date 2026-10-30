package co.edu.ucc.aiquota.quota;

import co.edu.ucc.aiquota.plan.SubscriptionPlan;
import java.time.Instant;
import java.time.LocalDate;

public record QuotaStatusResponse(
        String consumerId,
        String displayName,
        SubscriptionPlan plan,
        boolean unlimited,
        long tokensUsed,
        Long tokensRemaining,
        Long monthlyTokenLimit,
        LocalDate resetDate,
        int requestsUsedThisMinute,
        Integer requestsPerMinuteLimit,
        Instant rateLimitResetAt,
        long secondsUntilRateLimitReset,
        int expectedCompletionTokens
) {
}
