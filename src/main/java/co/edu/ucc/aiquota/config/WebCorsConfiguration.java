package co.edu.ucc.aiquota.config;

import co.edu.ucc.aiquota.web.ApiHeaders;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfiguration implements WebMvcConfigurer {

    private final String[] allowedOriginPatterns;

    public WebCorsConfiguration(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.allowedOriginPatterns = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(
                        HttpHeaders.RETRY_AFTER,
                        ApiHeaders.RATE_LIMIT_LIMIT,
                        ApiHeaders.RATE_LIMIT_REMAINING,
                        ApiHeaders.RATE_LIMIT_RESET,
                        ApiHeaders.QUOTA_REMAINING
                )
                .maxAge(3600);
    }
}
