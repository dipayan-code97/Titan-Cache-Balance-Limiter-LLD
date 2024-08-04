package titanVault.controller;


import titanVault.service.CircuitBreakerRateLimiter;

/**
 * Controller for handling requests and interfacing with the CircuitBreakerRateLimiter service.
 */
public class CircuitBreakerRateLimiterController {

    private final CircuitBreakerRateLimiter circuitBreakerRateLimiter;

    /**
     * Constructor for CircuitBreakerRateLimiterController.
     *
     * @param circuitBreakerRateLimiter The CircuitBreakerRateLimiter service instance.
     */
    public CircuitBreakerRateLimiterController(CircuitBreakerRateLimiter circuitBreakerRateLimiter) {
        this.circuitBreakerRateLimiter = circuitBreakerRateLimiter;
    }

    /**
     * Handles a request from a user and determines if it should be allowed.
     *
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     * @throws IllegalArgumentException if the userId is null or empty.
     */
    public boolean handleRequest(String userId) {
        if ((userId == null) || (userId.isEmpty())) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        return circuitBreakerRateLimiter.validateRequest(userId);
    }

    /**
     * Gets the retry-after duration in seconds if the rate limit is exceeded.
     *
     * @param userId The user identifier.
     * @return Retry-after duration in seconds.
     * @throws IllegalArgumentException if the userId is null or empty.
     */
    public long getRetryAfter(String userId) {
        if ((userId == null) || (userId.isEmpty())) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        return circuitBreakerRateLimiter.getRetryAfter(userId);
    }
}