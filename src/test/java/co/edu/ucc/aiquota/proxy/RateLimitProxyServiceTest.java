package co.edu.ucc.aiquota.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.ucc.aiquota.consumer.ConsumerAccount;
import co.edu.ucc.aiquota.consumer.ConsumerAccountService;
import co.edu.ucc.aiquota.generation.AIGenerationService;
import co.edu.ucc.aiquota.generation.GenerationRequest;
import co.edu.ucc.aiquota.generation.GenerationResponse;
import co.edu.ucc.aiquota.plan.SubscriptionPlan;
import co.edu.ucc.aiquota.ratelimit.RateLimitExceededException;
import co.edu.ucc.aiquota.ratelimit.RequestWindowTracker;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitProxyServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-30T23:00:15Z");

    private AIGenerationService nextService;
    private ConsumerAccountService consumerAccountService;
    private RequestWindowTracker requestWindowTracker;
    private RateLimitProxyService rateLimitProxyService;

    @BeforeEach
    void setUp() {
        nextService = mock(AIGenerationService.class);
        consumerAccountService = mock(ConsumerAccountService.class);
        requestWindowTracker = new RequestWindowTracker(Clock.fixed(NOW, ZoneId.of("America/Bogota")));
        rateLimitProxyService = new RateLimitProxyService(nextService, consumerAccountService, requestWindowTracker);
        when(nextService.generate(any())).thenReturn(new GenerationResponse("ok", 1, 1, 2, 0L, NOW));
    }

    @Test
    void blocksTheEleventhRequestOfAFreeConsumerWithinTheSameMinute() {
        givenConsumer("laura-free", SubscriptionPlan.FREE);
        GenerationRequest request = new GenerationRequest("laura-free", "Hola");

        for (int attempt = 0; attempt < 10; attempt++) {
            rateLimitProxyService.generate(request);
        }

        assertThatThrownBy(() -> rateLimitProxyService.generate(request))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(exception -> {
                    RateLimitExceededException rateLimit = (RateLimitExceededException) exception;
                    assertThat(rateLimit.getRequestsPerMinute()).isEqualTo(10);
                    assertThat(rateLimit.getRetryAfterSeconds()).isEqualTo(45L);
                });
        verify(nextService, times(10)).generate(request);
    }

    @Test
    void allowsRequestsAgainAfterTheWindowIsReset() {
        givenConsumer("laura-free", SubscriptionPlan.FREE);
        GenerationRequest request = new GenerationRequest("laura-free", "Hola");
        for (int attempt = 0; attempt < 10; attempt++) {
            rateLimitProxyService.generate(request);
        }

        requestWindowTracker.resetWindow();

        assertThat(rateLimitProxyService.generate(request).text()).isEqualTo("ok");
    }

    @Test
    void neverBlocksEnterpriseConsumers() {
        givenConsumer("andres-enterprise", SubscriptionPlan.ENTERPRISE);
        GenerationRequest request = new GenerationRequest("andres-enterprise", "Hola");

        for (int attempt = 0; attempt < 100; attempt++) {
            rateLimitProxyService.generate(request);
        }

        verify(nextService, times(100)).generate(request);
        assertThat(requestWindowTracker.requestsInCurrentWindow("andres-enterprise")).isEqualTo(100);
    }

    @Test
    void countsEachConsumerIndependently() {
        givenConsumer("laura-free", SubscriptionPlan.FREE);
        givenConsumer("mateo-free", SubscriptionPlan.FREE);
        for (int attempt = 0; attempt < 10; attempt++) {
            rateLimitProxyService.generate(new GenerationRequest("laura-free", "Hola"));
        }

        assertThat(rateLimitProxyService.generate(new GenerationRequest("mateo-free", "Hola")).text()).isEqualTo("ok");
    }

    private void givenConsumer(String consumerId, SubscriptionPlan plan) {
        when(consumerAccountService.findAccount(consumerId))
                .thenReturn(new ConsumerAccount(consumerId, consumerId, plan, 0L));
    }
}
