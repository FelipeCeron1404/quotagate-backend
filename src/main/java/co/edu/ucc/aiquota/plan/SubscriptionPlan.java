package co.edu.ucc.aiquota.plan;

public enum SubscriptionPlan {

    FREE(10, 50_000L, false),
    PRO(60, 500_000L, false),
    ENTERPRISE(0, 0L, true);

    private final int requestsPerMinute;
    private final long monthlyTokenLimit;
    private final boolean unlimited;

    SubscriptionPlan(int requestsPerMinute, long monthlyTokenLimit, boolean unlimited) {
        this.requestsPerMinute = requestsPerMinute;
        this.monthlyTokenLimit = monthlyTokenLimit;
        this.unlimited = unlimited;
    }

    public int requestsPerMinute() {
        return requestsPerMinute;
    }

    public long monthlyTokenLimit() {
        return monthlyTokenLimit;
    }

    public boolean isUnlimited() {
        return unlimited;
    }

    public boolean canUpgradeTo(SubscriptionPlan targetPlan) {
        return targetPlan != null && targetPlan.ordinal() > ordinal();
    }

    public long remainingTokens(long tokensUsed) {
        if (unlimited) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, monthlyTokenLimit - tokensUsed);
    }
}
