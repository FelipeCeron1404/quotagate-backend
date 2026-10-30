package co.edu.ucc.aiquota.consumer;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsumerAccountRepository extends JpaRepository<ConsumerAccount, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from ConsumerAccount account where account.id = :id")
    Optional<ConsumerAccount> findByIdForUpdate(@Param("id") String id);

    @Modifying
    @Query("update ConsumerAccount account set account.tokensUsedThisMonth = 0")
    int resetMonthlyUsageForAll();
}
