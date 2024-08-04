package titanVault.controller;

import titanVault.service.SlidingWindowLogRateLimiter;

/**
 * Controller for handling requests and interfacing with the SlidingWindowLogRateLimiter service.
 */
public class SlidingWindowLogRateLimiterController {

    private final SlidingWindowLogRateLimiter slidingWindowLogRateLimiter;

    /**
     * Constructor for SlidingWindowLogRateLimiterController.
     *
     * @param slidingWindowLogRateLimiter The SlidingWindowLogRateLimiter service instance.
     */
    public SlidingWindowLogRateLimiterController(SlidingWindowLogRateLimiter slidingWindowLogRateLimiter) {
        this.slidingWindowLogRateLimiter = slidingWindowLogRateLimiter;
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
        return slidingWindowLogRateLimiter.validateRequest(userId);
    }
}
