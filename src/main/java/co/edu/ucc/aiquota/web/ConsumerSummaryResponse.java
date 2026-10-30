package co.edu.ucc.aiquota.web;

import co.edu.ucc.aiquota.consumer.ConsumerAccount;
import co.edu.ucc.aiquota.plan.SubscriptionPlan;

public record ConsumerSummaryResponse(String id, String displayName, SubscriptionPlan plan) {

    public static ConsumerSummaryResponse from(ConsumerAccount account) {
        return new ConsumerSummaryResponse(account.getId(), account.getDisplayName(), account.getSubscriptionPlan());
    }
}
