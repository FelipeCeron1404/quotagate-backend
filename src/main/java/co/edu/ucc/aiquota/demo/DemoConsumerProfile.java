package co.edu.ucc.aiquota.demo;

import co.edu.ucc.aiquota.plan.SubscriptionPlan;

public record DemoConsumerProfile(
        String id,
        String displayName,
        SubscriptionPlan plan,
        long tokensUsedThisMonth,
        int minimumDailyTokens,
        int maximumDailyTokens
) {
}
