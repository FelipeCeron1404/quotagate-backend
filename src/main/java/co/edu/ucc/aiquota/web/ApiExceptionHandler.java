package co.edu.ucc.aiquota.web;

import co.edu.ucc.aiquota.consumer.ConsumerNotFoundException;
import co.edu.ucc.aiquota.generation.GenerationInterruptedException;
import co.edu.ucc.aiquota.quota.InvalidPlanUpgradeException;
import co.edu.ucc.aiquota.quota.QuotaExceededException;
import co.edu.ucc.aiquota.ratelimit.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final Clock clock;

    public ApiExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitExceededException exception,
                                                            HttpServletRequest request) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("retryAfterSeconds", exception.getRetryAfterSeconds());
        details.put("requestsPerMinuteLimit", exception.getRequestsPerMinute());
        String retryAfter = String.valueOf(exception.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, retryAfter)
                .header(ApiHeaders.RATE_LIMIT_LIMIT, String.valueOf(exception.getRequestsPerMinute()))
                .header(ApiHeaders.RATE_LIMIT_REMAINING, "0")
                .header(ApiHeaders.RATE_LIMIT_RESET, retryAfter)
                .body(errorBody(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage(), request, details));
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleQuota(QuotaExceededException exception,
                                                        HttpServletRequest request) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("plan", exception.getSubscriptionPlan());
        details.put("tokensRemaining", exception.getTokensRemaining());
        details.put("tokensRequired", exception.getTokensRequired());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .header(ApiHeaders.QUOTA_REMAINING, String.valueOf(exception.getTokensRemaining()))
                .body(errorBody(HttpStatus.PAYMENT_REQUIRED, exception.getMessage(), request, details));
    }

    @ExceptionHandler(ConsumerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleConsumerNotFound(ConsumerNotFoundException exception,
                                                                   HttpServletRequest request) {
        return respond(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidPlanUpgradeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidUpgrade(InvalidPlanUpgradeException exception,
                                                                 HttpServletRequest request) {
        return respond(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingHeader(MissingRequestHeaderException exception,
                                                                HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST,
                "Falta el encabezado " + exception.getHeaderName() + " que identifica al usuario",
                request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                             HttpServletRequest request) {
        Map<String, Object> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fieldError -> String.valueOf(fieldError.getDefaultMessage()),
                        (first, second) -> first,
                        LinkedHashMap::new));
        return respond(HttpStatus.BAD_REQUEST, "La solicitud tiene datos inválidos", request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception,
                                                                 HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no es un JSON válido o contiene un valor no permitido",
                request, Map.of());
    }

    @ExceptionHandler(GenerationInterruptedException.class)
    public ResponseEntity<ApiErrorResponse> handleInterruptedGeneration(GenerationInterruptedException exception,
                                                                        HttpServletRequest request) {
        return respond(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> respond(HttpStatus status,
                                                     String message,
                                                     HttpServletRequest request,
                                                     Map<String, Object> details) {
        return ResponseEntity.status(status).body(errorBody(status, message, request, details));
    }

    private ApiErrorResponse errorBody(HttpStatus status,
                                       String message,
                                       HttpServletRequest request,
                                       Map<String, Object> details) {
        return new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                Instant.now(clock),
                details
        );
    }
}
