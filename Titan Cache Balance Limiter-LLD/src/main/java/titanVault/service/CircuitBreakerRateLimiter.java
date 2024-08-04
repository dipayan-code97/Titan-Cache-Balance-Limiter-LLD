package titanVault.service;

import titanVault.model.BurstRateBucket;
import titanVault.model.CircuitBreaker;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CircuitBreakerRateLimiter combines rate limiting with a circuit breaker pattern.
 * It uses BurstRateBucket to manage request rate and CircuitBreaker to handle service failures.
 */
public class CircuitBreakerRateLimiter implements RateLimiter {

    private final BurstRateBucket burstRateBucket; // Instance of BurstRateBucket for rate limiting
    private final CircuitBreaker circuitBreaker; // Instance of CircuitBreaker for handling service failures
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Lock for thread safety

    /**
     * Constructor to initialize the CircuitBreakerRateLimiter.
     *
     * @param refillRate The rate at which tokens are added to the bucket (tokens per second).
     * @param burstCapacity The maximum number of tokens that the bucket can hold.
     * @param failureThreshold The number of failures that trigger the circuit breaker to open.
     * @param resetTimeoutMillis Time in milliseconds to wait before transitioning from OPEN to HALF_OPEN.
     */
    public CircuitBreakerRateLimiter(long refillRate, long burstCapacity,
                                     long failureThreshold, long resetTimeoutMillis) {
        this.burstRateBucket = new BurstRateBucket(refillRate, burstCapacity); // Initialize rate limiter
        this.circuitBreaker = new CircuitBreaker(failureThreshold, resetTimeoutMillis); // Initialize circuit breaker
    }

    /**
     * Validates if a request is allowed based on rate limiting and circuit breaker policies.
     *
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    @Override
    public boolean validateRequest(String userId) {
        readWriteLock.readLock().lock(); // Acquire read lock
        try {
            // Check if the circuit breaker allows the call.
            if (!circuitBreaker.isCallAllowed()) {
                return false; // Call is not allowed if circuit breaker is OPEN or not in HALF_OPEN
            }

            // Validate the request with the rate limiter.
            boolean isAllowed = burstRateBucket.validateRequest(userId);
            if (isAllowed) {
                // Record success if the request is allowed.
                circuitBreaker.recordSuccess();
            } else {
                // Record failure if the request is not allowed.
                circuitBreaker.recordFailure();
            }
            return isAllowed;
        } finally {
            readWriteLock.readLock().unlock(); // Release read lock
        }
    }

    /**
     * Gets the retry-after duration in seconds if the rate limit is exceeded.
     *
     * @param userId The user identifier.
     * @return Retry-after duration in seconds.
     */
    public long getRetryAfter(String userId) {
        readWriteLock.readLock().lock(); // Acquire read lock
        try {
            return burstRateBucket.getRetryAfter(userId); // Get retry after duration from rate limiter
        } finally {
            readWriteLock.readLock().unlock(); // Release read lock
        }
    }
}
