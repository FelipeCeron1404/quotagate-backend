package co.edu.ucc.aiquota.proxy;

import co.edu.ucc.aiquota.consumer.ConsumerAccountService;
import co.edu.ucc.aiquota.generation.AIGenerationService;
import co.edu.ucc.aiquota.generation.MockAIGenerationService;
import co.edu.ucc.aiquota.generation.TokenEstimator;
import co.edu.ucc.aiquota.quota.QuotaLedgerService;
import co.edu.ucc.aiquota.ratelimit.RequestWindowTracker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GenerationProxyChainConfiguration {

    @Bean
    public QuotaProxyService quotaProxyService(MockAIGenerationService mockAIGenerationService,
                                               QuotaLedgerService quotaLedgerService,
                                               TokenEstimator tokenEstimator) {
        return new QuotaProxyService(mockAIGenerationService, quotaLedgerService, tokenEstimator);
    }

    @Bean
    @Primary
    public AIGenerationService rateLimitProxyService(QuotaProxyService quotaProxyService,
                                                     ConsumerAccountService consumerAccountService,
                                                     RequestWindowTracker requestWindowTracker) {
        return new RateLimitProxyService(quotaProxyService, consumerAccountService, requestWindowTracker);
    }
}
