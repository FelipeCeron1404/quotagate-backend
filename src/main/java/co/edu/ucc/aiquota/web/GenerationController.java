package co.edu.ucc.aiquota.web;

import co.edu.ucc.aiquota.generation.AIGenerationService;
import co.edu.ucc.aiquota.generation.GenerationRequest;
import co.edu.ucc.aiquota.generation.GenerationResponse;
import co.edu.ucc.aiquota.quota.QuotaStatusResponse;
import co.edu.ucc.aiquota.quota.QuotaStatusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class GenerationController {

    private final AIGenerationService aiGenerationService;
    private final QuotaStatusService quotaStatusService;

    public GenerationController(AIGenerationService aiGenerationService, QuotaStatusService quotaStatusService) {
        this.aiGenerationService = aiGenerationService;
        this.quotaStatusService = quotaStatusService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generate(@RequestHeader(ApiHeaders.CONSUMER_ID) String consumerId,
                                                       @Valid @RequestBody PromptRequest promptRequest) {
        GenerationResponse response = aiGenerationService.generate(new GenerationRequest(consumerId, promptRequest.prompt()));
        QuotaStatusResponse status = quotaStatusService.statusOf(consumerId);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .header(ApiHeaders.RATE_LIMIT_RESET, String.valueOf(status.secondsUntilRateLimitReset()));
        if (!status.unlimited()) {
            builder.header(ApiHeaders.RATE_LIMIT_LIMIT, String.valueOf(status.requestsPerMinuteLimit()))
                    .header(ApiHeaders.RATE_LIMIT_REMAINING,
                            String.valueOf(Math.max(0, status.requestsPerMinuteLimit() - status.requestsUsedThisMinute())))
                    .header(ApiHeaders.QUOTA_REMAINING, String.valueOf(status.tokensRemaining()));
        }
        return builder.body(response);
    }
}
