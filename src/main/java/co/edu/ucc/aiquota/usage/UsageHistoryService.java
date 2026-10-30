package co.edu.ucc.aiquota.usage;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsageHistoryService {

    public static final int HISTORY_DAYS = 7;

    private final DailyUsageRepository dailyUsageRepository;
    private final Clock clock;

    public UsageHistoryService(DailyUsageRepository dailyUsageRepository, Clock clock) {
        this.dailyUsageRepository = dailyUsageRepository;
        this.clock = clock;
    }

    @Transactional
    public void recordUsage(String consumerId, long tokens) {
        LocalDate today = LocalDate.now(clock);
        DailyUsage dailyUsage = dailyUsageRepository.findByConsumerIdAndUsageDate(consumerId, today)
                .orElseGet(() -> new DailyUsage(consumerId, today));
        dailyUsage.registerRequest(tokens);
        dailyUsageRepository.save(dailyUsage);
    }

    @Transactional(readOnly = true)
    public List<DailyUsagePoint> lastSevenDays(String consumerId) {
        LocalDate today = LocalDate.now(clock);
        LocalDate firstDay = today.minusDays(HISTORY_DAYS - 1L);
        Map<LocalDate, DailyUsage> usageByDate = dailyUsageRepository
                .findByConsumerIdAndUsageDateBetweenOrderByUsageDateAsc(consumerId, firstDay, today)
                .stream()
                .collect(Collectors.toMap(DailyUsage::getUsageDate, Function.identity()));
        List<DailyUsagePoint> history = new ArrayList<>(HISTORY_DAYS);
        for (LocalDate day = firstDay; !day.isAfter(today); day = day.plusDays(1)) {
            DailyUsage dailyUsage = usageByDate.get(day);
            history.add(dailyUsage == null
                    ? new DailyUsagePoint(day, 0L, 0)
                    : new DailyUsagePoint(day, dailyUsage.getTokensConsumed(), dailyUsage.getRequestCount()));
        }
        return history;
    }
}
