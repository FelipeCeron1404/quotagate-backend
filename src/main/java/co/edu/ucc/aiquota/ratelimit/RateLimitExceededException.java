package co.edu.ucc.aiquota.ratelimit;

public class RateLimitExceededException extends RuntimeException {

    private final int requestsPerMinute;
    private final long retryAfterSeconds;

    public RateLimitExceededException(int requestsPerMinute, long retryAfterSeconds) {
        super("Superaste el límite de " + requestsPerMinute
                + " solicitudes por minuto. Intenta de nuevo en " + retryAfterSeconds + " segundos");
        this.requestsPerMinute = requestsPerMinute;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
