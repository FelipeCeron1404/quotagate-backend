package co.edu.ucc.aiquota.quota;

import co.edu.ucc.aiquota.consumer.ConsumerAccount;
import co.edu.ucc.aiquota.consumer.ConsumerAccountRepository;
import co.edu.ucc.aiquota.consumer.ConsumerNotFoundException;
import co.edu.ucc.aiquota.usage.UsageHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuotaLedgerService {

    private final ConsumerAccountRepository consumerAccountRepository;
    private final UsageHistoryService usageHistoryService;

    public QuotaLedgerService(ConsumerAccountRepository consumerAccountRepository,
                              UsageHistoryService usageHistoryService) {
        this.consumerAccountRepository = consumerAccountRepository;
        this.usageHistoryService = usageHistoryService;
    }

    @Transactional(readOnly = true)
    public void ensureAffordable(String consumerId, long tokensRequired) {
        ConsumerAccount account = consumerAccountRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        long minimumRequired = Math.max(1L, tokensRequired);
        if (!account.canAfford(minimumRequired)) {
            throw new QuotaExceededException(account.getSubscriptionPlan(), account.remainingTokens(), minimumRequired);
        }
    }

    @Transactional
    public void charge(String consumerId, long tokensConsumed) {
        ConsumerAccount account = consumerAccountRepository.findByIdForUpdate(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        account.consumeTokens(tokensConsumed);
        usageHistoryService.recordUsage(consumerId, tokensConsumed);
    }

    @Transactional
    public int resetMonthlyUsage() {
        return consumerAccountRepository.resetMonthlyUsageForAll();
    }
}
