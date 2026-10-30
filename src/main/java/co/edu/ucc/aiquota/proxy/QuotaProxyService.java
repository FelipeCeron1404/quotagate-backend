package co.edu.ucc.aiquota.proxy;

import co.edu.ucc.aiquota.generation.AIGenerationService;
import co.edu.ucc.aiquota.generation.GenerationRequest;
import co.edu.ucc.aiquota.generation.GenerationResponse;
import co.edu.ucc.aiquota.generation.TokenEstimator;
import co.edu.ucc.aiquota.quota.QuotaLedgerService;

public class QuotaProxyService implements AIGenerationService {

    private final AIGenerationService nextService;
    private final QuotaLedgerService quotaLedgerService;
    private final TokenEstimator tokenEstimator;

    public QuotaProxyService(AIGenerationService nextService,
                             QuotaLedgerService quotaLedgerService,
                             TokenEstimator tokenEstimator) {
        this.nextService = nextService;
        this.quotaLedgerService = quotaLedgerService;
        this.tokenEstimator = tokenEstimator;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        int promptTokens = tokenEstimator.estimate(request.prompt());
        quotaLedgerService.ensureAffordable(request.consumerId(), promptTokens);
        GenerationResponse response = nextService.generate(request);
        quotaLedgerService.charge(request.consumerId(), response.totalTokens());
        return response;
    }
}
