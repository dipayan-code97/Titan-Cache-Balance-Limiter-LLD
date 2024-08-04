package titanVault.controller;

import titanVault.service.EMABucketServiceRateLimiter;

import java.time.Duration;

/**
 * Controller for handling requests and interfacing with the EMABucketRateLimiter service.
 */
public class EMABucketRateLimiterController {

    private final EMABucketServiceRateLimiter emaBucketServiceRateLimiter;

    /**
     * Constructor to initialize the controller with the rate limiter.
     *
     * @param rateLimiter The rate limiter service to interact with.
     */
    public EMABucketRateLimiterController(EMABucketServiceRateLimiter rateLimiter) {
        this.emaBucketServiceRateLimiter = rateLimiter;
    }

    /**
     * Validates a request for a given userId.
     *
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean validateRequest(String userId) {
        return emaBucketServiceRateLimiter.validateRequest(userId);
    }

    /**
     * Updates the rate limit configuration for all users.
     *
     * @param newMaxCapacity     New maximum number of requests allowed.
     * @param newAveragingPeriod New duration over which EMA is calculated.
     */
    public void updateConfiguration(long newMaxCapacity, Duration newAveragingPeriod) {
        emaBucketServiceRateLimiter.updateConfiguration(newMaxCapacity, newAveragingPeriod);
    }
}
