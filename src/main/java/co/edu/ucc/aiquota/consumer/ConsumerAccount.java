package co.edu.ucc.aiquota.consumer;

import co.edu.ucc.aiquota.plan.SubscriptionPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "consumer_accounts")
public class ConsumerAccount {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false, length = 20)
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "tokens_used_this_month", nullable = false)
    private long tokensUsedThisMonth;

    protected ConsumerAccount() {
    }

    public ConsumerAccount(String id, String displayName, SubscriptionPlan subscriptionPlan, long tokensUsedThisMonth) {
        this.id = id;
        this.displayName = displayName;
        this.subscriptionPlan = subscriptionPlan;
        this.tokensUsedThisMonth = tokensUsedThisMonth;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public long getTokensUsedThisMonth() {
        return tokensUsedThisMonth;
    }

    public long remainingTokens() {
        return subscriptionPlan.remainingTokens(tokensUsedThisMonth);
    }

    public boolean canAfford(long tokens) {
        return subscriptionPlan.isUnlimited() || remainingTokens() >= tokens;
    }

    public void consumeTokens(long tokens) {
        long updatedUsage = tokensUsedThisMonth + tokens;
        if (!subscriptionPlan.isUnlimited()) {
            updatedUsage = Math.min(updatedUsage, subscriptionPlan.monthlyTokenLimit());
        }
        tokensUsedThisMonth = updatedUsage;
    }

    public void changePlan(SubscriptionPlan targetPlan) {
        subscriptionPlan = targetPlan;
    }

    public void resetMonthlyUsage() {
        tokensUsedThisMonth = 0L;
    }
}
