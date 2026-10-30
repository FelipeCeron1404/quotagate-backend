package co.edu.ucc.aiquota.plan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SubscriptionPlanTest {

    @Test
    void freePlanAllowsTenRequestsAndFiftyThousandTokens() {
        assertThat(SubscriptionPlan.FREE.requestsPerMinute()).isEqualTo(10);
        assertThat(SubscriptionPlan.FREE.monthlyTokenLimit()).isEqualTo(50_000L);
    }

    @Test
    void proPlanAllowsSixtyRequestsAndFiveHundredThousandTokens() {
        assertThat(SubscriptionPlan.PRO.requestsPerMinute()).isEqualTo(60);
        assertThat(SubscriptionPlan.PRO.monthlyTokenLimit()).isEqualTo(500_000L);
    }

    @Test
    void enterprisePlanIsUnlimited() {
        assertThat(SubscriptionPlan.ENTERPRISE.isUnlimited()).isTrue();
        assertThat(SubscriptionPlan.ENTERPRISE.remainingTokens(9_999_999L)).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void remainingTokensNeverGoesBelowZero() {
        assertThat(SubscriptionPlan.FREE.remainingTokens(60_000L)).isZero();
    }

    @Test
    void onlyUpgradesToHigherPlansAreAllowed() {
        assertThat(SubscriptionPlan.FREE.canUpgradeTo(SubscriptionPlan.PRO)).isTrue();
        assertThat(SubscriptionPlan.PRO.canUpgradeTo(SubscriptionPlan.ENTERPRISE)).isTrue();
        assertThat(SubscriptionPlan.PRO.canUpgradeTo(SubscriptionPlan.FREE)).isFalse();
        assertThat(SubscriptionPlan.FREE.canUpgradeTo(SubscriptionPlan.FREE)).isFalse();
    }
}
