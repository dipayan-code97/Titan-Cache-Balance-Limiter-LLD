package titanVault.controller;

import titanVault.service.BurstRateServiceLimiter;

/**
 * Controller for handling requests and interfacing with the BurstRateServiceLimiter service.
 */
public class BurstRateLimiterController {

    private final BurstRateServiceLimiter burstRateServiceLimiter;

    /**
     * Constructor for BurstRateLimiterController.
     *
     * @param burstRateServiceLimiter The BurstRateServiceLimiter service instance.
     */
    public BurstRateLimiterController(BurstRateServiceLimiter burstRateServiceLimiter) {
        this.burstRateServiceLimiter = burstRateServiceLimiter;
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
        return burstRateServiceLimiter.consumeToken(userId);
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
        return burstRateServiceLimiter.getRetryAfter(userId);
    }
}