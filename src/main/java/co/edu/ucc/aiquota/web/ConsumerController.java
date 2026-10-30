package co.edu.ucc.aiquota.web;

import co.edu.ucc.aiquota.consumer.ConsumerAccountService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consumers")
public class ConsumerController {

    private final ConsumerAccountService consumerAccountService;

    public ConsumerController(ConsumerAccountService consumerAccountService) {
        this.consumerAccountService = consumerAccountService;
    }

    @GetMapping
    public List<ConsumerSummaryResponse> listConsumers() {
        return consumerAccountService.listAccounts().stream()
                .map(ConsumerSummaryResponse::from)
                .toList();
    }
}
