package co.edu.ucc.aiquota.quota;

import co.edu.ucc.aiquota.plan.SubscriptionPlan;

public class QuotaExceededException extends RuntimeException {

    private final SubscriptionPlan subscriptionPlan;
    private final long tokensRemaining;
    private final long tokensRequired;

    public QuotaExceededException(SubscriptionPlan subscriptionPlan, long tokensRemaining, long tokensRequired) {
        super("Agotaste la cuota mensual de tokens del plan " + subscriptionPlan
                + ". Te quedan " + tokensRemaining + " tokens y la solicitud requiere " + tokensRequired);
        this.subscriptionPlan = subscriptionPlan;
        this.tokensRemaining = tokensRemaining;
        this.tokensRequired = tokensRequired;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public long getTokensRemaining() {
        return tokensRemaining;
    }

    public long getTokensRequired() {
        return tokensRequired;
    }
}
