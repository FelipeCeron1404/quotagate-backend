package co.edu.ucc.aiquota.generation;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class MockAIGenerationService implements AIGenerationService {

    private static final long SIMULATED_LATENCY_MS = 1200L;

    private final GeneratedTextCatalog generatedTextCatalog;
    private final TokenEstimator tokenEstimator;
    private final Clock clock;

    public MockAIGenerationService(GeneratedTextCatalog generatedTextCatalog,
                                   TokenEstimator tokenEstimator,
                                   Clock clock) {
        this.generatedTextCatalog = generatedTextCatalog;
        this.tokenEstimator = tokenEstimator;
        this.clock = clock;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        long startedAt = System.currentTimeMillis();
        simulateModelLatency();
        String text = generatedTextCatalog.randomText();
        int promptTokens = tokenEstimator.estimate(request.prompt());
        int completionTokens = tokenEstimator.estimate(text);
        long latencyMs = System.currentTimeMillis() - startedAt;
        return new GenerationResponse(
                text,
                promptTokens,
                completionTokens,
                promptTokens + completionTokens,
                latencyMs,
                Instant.now(clock)
        );
    }

    private void simulateModelLatency() {
        try {
            Thread.sleep(SIMULATED_LATENCY_MS);
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw new GenerationInterruptedException();
        }
    }
}
