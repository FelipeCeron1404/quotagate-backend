package co.edu.ucc.aiquota.quota;

import co.edu.ucc.aiquota.consumer.ConsumerAccount;
import co.edu.ucc.aiquota.consumer.ConsumerAccountRepository;
import co.edu.ucc.aiquota.consumer.ConsumerNotFoundException;
import co.edu.ucc.aiquota.plan.SubscriptionPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanUpgradeService {

    private final ConsumerAccountRepository consumerAccountRepository;

    public PlanUpgradeService(ConsumerAccountRepository consumerAccountRepository) {
        this.consumerAccountRepository = consumerAccountRepository;
    }

    @Transactional
    public void upgrade(String consumerId, SubscriptionPlan targetPlan) {
        ConsumerAccount account = consumerAccountRepository.findByIdForUpdate(consumerId)
                .orElseThrow(() -> new ConsumerNotFoundException(consumerId));
        SubscriptionPlan currentPlan = account.getSubscriptionPlan();
        if (!currentPlan.canUpgradeTo(targetPlan)) {
            throw new InvalidPlanUpgradeException(currentPlan, targetPlan);
        }
        account.changePlan(targetPlan);
    }
}
