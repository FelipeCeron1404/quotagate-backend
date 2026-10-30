package co.edu.ucc.aiquota.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.ucc.aiquota.generation.AIGenerationService;
import co.edu.ucc.aiquota.generation.GenerationRequest;
import co.edu.ucc.aiquota.generation.GenerationResponse;
import co.edu.ucc.aiquota.generation.TokenEstimator;
import co.edu.ucc.aiquota.plan.SubscriptionPlan;
import co.edu.ucc.aiquota.quota.QuotaExceededException;
import co.edu.ucc.aiquota.quota.QuotaLedgerService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class QuotaProxyServiceTest {

    private AIGenerationService nextService;
    private QuotaLedgerService quotaLedgerService;
    private QuotaProxyService quotaProxyService;

    @BeforeEach
    void setUp() {
        nextService = mock(AIGenerationService.class);
        quotaLedgerService = mock(QuotaLedgerService.class);
        quotaProxyService = new QuotaProxyService(nextService, quotaLedgerService, new TokenEstimator());
    }

    @Test
    void chargesTheTotalTokensAfterASuccessfulGeneration() {
        GenerationRequest request = new GenerationRequest("sofia-pro", "Explica el patrón Proxy");
        when(nextService.generate(request)).thenReturn(new GenerationResponse("texto", 6, 40, 46, 1200L, Instant.now()));

        GenerationResponse response = quotaProxyService.generate(request);

        assertThat(response.totalTokens()).isEqualTo(46);
        InOrder order = inOrder(quotaLedgerService, nextService);
        order.verify(quotaLedgerService).ensureAffordable("sofia-pro", 6L);
        order.verify(nextService).generate(request);
        order.verify(quotaLedgerService).charge("sofia-pro", 46L);
    }

    @Test
    void doesNotCallTheRealServiceWhenTheQuotaIsExhausted() {
        GenerationRequest request = new GenerationRequest("mateo-free", "Hola");
        doThrow(new QuotaExceededException(SubscriptionPlan.FREE, 0L, 1L))
                .when(quotaLedgerService).ensureAffordable(eq("mateo-free"), anyLong());

        assertThatThrownBy(() -> quotaProxyService.generate(request))
                .isInstanceOf(QuotaExceededException.class);
        verify(nextService, never()).generate(any());
        verify(quotaLedgerService, never()).charge(any(), anyLong());
    }
}
