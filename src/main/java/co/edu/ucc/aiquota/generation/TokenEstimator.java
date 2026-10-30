package co.edu.ucc.aiquota.generation;

import org.springframework.stereotype.Component;

@Component
public class TokenEstimator {

    private static final int CHARACTERS_PER_TOKEN = 4;

    public int estimate(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        int characters = text.trim().length();
        return Math.max(1, (characters + CHARACTERS_PER_TOKEN - 1) / CHARACTERS_PER_TOKEN);
    }
}
