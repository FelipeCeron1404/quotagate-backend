package co.edu.ucc.aiquota.usage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyUsageRepository extends JpaRepository<DailyUsage, Long> {

    Optional<DailyUsage> findByConsumerIdAndUsageDate(String consumerId, LocalDate usageDate);

    List<DailyUsage> findByConsumerIdAndUsageDateBetweenOrderByUsageDateAsc(String consumerId,
                                                                           LocalDate from,
                                                                           LocalDate to);
}
