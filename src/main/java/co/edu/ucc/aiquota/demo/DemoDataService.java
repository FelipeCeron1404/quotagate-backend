package co.edu.ucc.aiquota.demo;

import co.edu.ucc.aiquota.consumer.ConsumerAccount;
import co.edu.ucc.aiquota.consumer.ConsumerAccountRepository;
import co.edu.ucc.aiquota.plan.SubscriptionPlan;
import co.edu.ucc.aiquota.ratelimit.RequestWindowTracker;
import co.edu.ucc.aiquota.usage.DailyUsage;
import co.edu.ucc.aiquota.usage.DailyUsageRepository;
import co.edu.ucc.aiquota.usage.UsageHistoryService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoDataService {

    private static final int AVERAGE_TOKENS_PER_REQUEST = 180;

    private static final List<DemoConsumerProfile> DEMO_CONSUMERS = List.of(
            new DemoConsumerProfile("laura-free", "Laura Gómez", SubscriptionPlan.FREE, 12_400L, 300, 2_500),
            new DemoConsumerProfile("mateo-free", "Mateo Rosero", SubscriptionPlan.FREE, 49_650L, 800, 3_200),
            new DemoConsumerProfile("sofia-pro", "Sofía Burbano", SubscriptionPlan.PRO, 186_300L, 4_000, 15_000),
            new DemoConsumerProfile("andres-enterprise", "Andrés Erazo", SubscriptionPlan.ENTERPRISE, 1_248_900L, 20_000, 60_000)
    );

    private final ConsumerAccountRepository consumerAccountRepository;
    private final DailyUsageRepository dailyUsageRepository;
    private final RequestWindowTracker requestWindowTracker;
    private final Clock clock;

    public DemoDataService(ConsumerAccountRepository consumerAccountRepository,
                           DailyUsageRepository dailyUsageRepository,
                           RequestWindowTracker requestWindowTracker,
                           Clock clock) {
        this.consumerAccountRepository = consumerAccountRepository;
        this.dailyUsageRepository = dailyUsageRepository;
        this.requestWindowTracker = requestWindowTracker;
        this.clock = clock;
    }

    @Transactional
    public void seedIfEmpty() {
        if (consumerAccountRepository.count() == 0) {
            restoreDemoData();
        }
    }

    @Transactional
    public void restoreDemoData() {
        dailyUsageRepository.deleteAllInBatch();
        consumerAccountRepository.deleteAllInBatch();
        LocalDate today = LocalDate.now(clock);
        List<DailyUsage> history = new ArrayList<>();
        for (DemoConsumerProfile profile : DEMO_CONSUMERS) {
            long tokensInCurrentMonth = 0L;
            Random random = new Random(profile.id().hashCode());
            for (int daysAgo = UsageHistoryService.HISTORY_DAYS - 1; daysAgo >= 1; daysAgo--) {
                LocalDate day = today.minusDays(daysAgo);
                long tokens = profile.minimumDailyTokens()
                        + random.nextInt(profile.maximumDailyTokens() - profile.minimumDailyTokens() + 1);
                int requests = (int) Math.max(1L, tokens / AVERAGE_TOKENS_PER_REQUEST);
                history.add(new DailyUsage(profile.id(), day, tokens, requests));
                if (day.getMonth() == today.getMonth() && day.getYear() == today.getYear()) {
                    tokensInCurrentMonth += tokens;
                }
            }
            long tokensUsed = Math.max(profile.tokensUsedThisMonth(), tokensInCurrentMonth);
            if (!profile.plan().isUnlimited()) {
                tokensUsed = Math.min(tokensUsed, profile.plan().monthlyTokenLimit());
            }
            consumerAccountRepository.save(new ConsumerAccount(profile.id(), profile.displayName(), profile.plan(), tokensUsed));
        }
        dailyUsageRepository.saveAll(history);
        requestWindowTracker.resetWindow();
    }
}
