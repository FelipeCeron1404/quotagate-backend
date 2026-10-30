package co.edu.ucc.aiquota.consumer;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumerAccountService {

    private final ConsumerAccountRepository consumerAccountRepository;

    public ConsumerAccountService(ConsumerAccountRepository consumerAccountRepository) {
        this.consumerAccountRepository = consumerAccountRepository;
    }

    @Transactional(readOnly = true)
    public ConsumerAccount findAccount(String consumerId) {
        return consumerAccountRepository.findById(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException(consumerId));
    }

    @Transactional(readOnly = true)
    public List<ConsumerAccount> listAccounts() {
        return consumerAccountRepository.findAll().stream()
                .sorted(Comparator.comparing(ConsumerAccount::getSubscriptionPlan)
                        .thenComparing(ConsumerAccount::getDisplayName))
                .toList();
    }
}
