package co.edu.ucc.aiquota.generation;

import java.time.Instant;

public record GenerationResponse(
        String text,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        long latencyMs,
        Instant generatedAt
) {
}
