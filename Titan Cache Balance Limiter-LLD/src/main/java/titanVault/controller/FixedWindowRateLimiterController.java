package titanVault.controller;

import titanVault.service.FixedWindowServiceRateLimiter;

import java.time.Duration;

public class FixedWindowRateLimiterController {

    /**
     * Controller for managing the Fixed Window Rate Limiter.
     */
    private final FixedWindowServiceRateLimiter fixedWindowServiceRateLimiter;

    /**
     * Constructor to initialize the Fixed Window Rate Limiter controller.
     *
     * @param fixedWindowServiceRateLimiter The Fixed Window Rate Limiter service to interact with.
     */
    public FixedWindowRateLimiterController(FixedWindowServiceRateLimiter fixedWindowServiceRateLimiter) {
        this.fixedWindowServiceRateLimiter = fixedWindowServiceRateLimiter;
    }

    /**
     * Validates a request for a given userId.
     *
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean validateRequest(String userId) {
        return fixedWindowServiceRateLimiter.validateRequest(userId);
    }

    /**
     * Updates the rate limiter configuration (max capacity and window interval).
     *
     * @param newMaxCapacity New maximum number of requests allowed in the window.
     * @param newWindowInterval New duration of the window.
     */
    public void updateConfiguration(long newMaxCapacity, Duration newWindowInterval) {
        fixedWindowServiceRateLimiter.updateConfiguration(newMaxCapacity, newWindowInterval);
    }
}
