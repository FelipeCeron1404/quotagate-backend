package co.edu.ucc.aiquota.proxy;

import co.edu.ucc.aiquota.consumer.ConsumerAccountService;
import co.edu.ucc.aiquota.generation.AIGenerationService;
import co.edu.ucc.aiquota.generation.GenerationRequest;
import co.edu.ucc.aiquota.generation.GenerationResponse;
import co.edu.ucc.aiquota.plan.SubscriptionPlan;
import co.edu.ucc.aiquota.ratelimit.RateLimitExceededException;
import co.edu.ucc.aiquota.ratelimit.RequestWindowTracker;

public class RateLimitProxyService implements AIGenerationService {

    private final AIGenerationService nextService;
    private final ConsumerAccountService consumerAccountService;
    private final RequestWindowTracker requestWindowTracker;

    public RateLimitProxyService(AIGenerationService nextService,
                                 ConsumerAccountService consumerAccountService,
                                 RequestWindowTracker requestWindowTracker) {
        this.nextService = nextService;
        this.consumerAccountService = consumerAccountService;
        this.requestWindowTracker = requestWindowTracker;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        SubscriptionPlan subscriptionPlan = consumerAccountService.findAccount(request.consumerId()).getSubscriptionPlan();
        if (subscriptionPlan.isUnlimited()) {
            requestWindowTracker.registerUnlimitedRequest(request.consumerId());
            return nextService.generate(request);
        }
        int requestsPerMinute = subscriptionPlan.requestsPerMinute();
        if (!requestWindowTracker.tryRegisterRequest(request.consumerId(), requestsPerMinute)) {
            throw new RateLimitExceededException(requestsPerMinute, requestWindowTracker.secondsUntilReset());
        }
        return nextService.generate(request);
    }
}
