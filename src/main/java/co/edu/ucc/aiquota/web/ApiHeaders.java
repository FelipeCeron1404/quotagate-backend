package co.edu.ucc.aiquota.web;

public final class ApiHeaders {

    public static final String CONSUMER_ID = "X-User-Id";
    public static final String RATE_LIMIT_LIMIT = "X-RateLimit-Limit";
    public static final String RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    public static final String RATE_LIMIT_RESET = "X-RateLimit-Reset";
    public static final String QUOTA_REMAINING = "X-Quota-Remaining";

    private ApiHeaders() {
    }
}
