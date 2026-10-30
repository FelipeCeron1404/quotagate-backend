package co.edu.ucc.aiquota.demo;

import co.edu.ucc.aiquota.consumer.ConsumerAccountService;
import co.edu.ucc.aiquota.web.ConsumerSummaryResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoController {

    private final DemoDataService demoDataService;
    private final ConsumerAccountService consumerAccountService;

    public DemoController(DemoDataService demoDataService, ConsumerAccountService consumerAccountService) {
        this.demoDataService = demoDataService;
        this.consumerAccountService = consumerAccountService;
    }

    @PostMapping("/reset")
    public List<ConsumerSummaryResponse> resetDemoData() {
        demoDataService.restoreDemoData();
        return consumerAccountService.listAccounts().stream()
                .map(ConsumerSummaryResponse::from)
                .toList();
    }
}
