package co.edu.ucc.aiquota.web;

import co.edu.ucc.aiquota.consumer.ConsumerAccountService;
import co.edu.ucc.aiquota.quota.PlanUpgradeService;
import co.edu.ucc.aiquota.quota.QuotaStatusResponse;
import co.edu.ucc.aiquota.quota.QuotaStatusService;
import co.edu.ucc.aiquota.usage.DailyUsagePoint;
import co.edu.ucc.aiquota.usage.UsageHistoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quota")
public class QuotaController {

    private final QuotaStatusService quotaStatusService;
    private final UsageHistoryService usageHistoryService;
    private final PlanUpgradeService planUpgradeService;
    private final ConsumerAccountService consumerAccountService;

    public QuotaController(QuotaStatusService quotaStatusService,
                           UsageHistoryService usageHistoryService,
                           PlanUpgradeService planUpgradeService,
                           ConsumerAccountService consumerAccountService) {
        this.quotaStatusService = quotaStatusService;
        this.usageHistoryService = usageHistoryService;
        this.planUpgradeService = planUpgradeService;
        this.consumerAccountService = consumerAccountService;
    }

    @GetMapping("/status")
    public QuotaStatusResponse status(@RequestHeader(ApiHeaders.CONSUMER_ID) String consumerId) {
        return quotaStatusService.statusOf(consumerId);
    }

    @GetMapping("/history")
    public List<DailyUsagePoint> history(@RequestHeader(ApiHeaders.CONSUMER_ID) String consumerId) {
        consumerAccountService.findAccount(consumerId);
        return usageHistoryService.lastSevenDays(consumerId);
    }

    @PostMapping("/upgrade")
    public QuotaStatusResponse upgrade(@RequestHeader(ApiHeaders.CONSUMER_ID) String consumerId,
                                       @Valid @RequestBody UpgradePlanRequest upgradePlanRequest) {
        planUpgradeService.upgrade(consumerId, upgradePlanRequest.targetPlan());
        return quotaStatusService.statusOf(consumerId);
    }
}
