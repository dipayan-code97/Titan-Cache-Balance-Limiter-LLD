package titanVault.controller;

import titanVault.service.AdaptiveBucketServiceRateLimiter;

/**
 * Controller for managing requests and user-specific rate limits using AdaptiveBucketRateLimiter.
 */
public class AdaptiveBucketRateLimiterController {

    private final AdaptiveBucketServiceRateLimiter adaptiveBucketServiceRateLimiter;

    /**
     * Constructor for AdaptiveBucketRateLimiterController.
     *
     * @param adaptiveBucketServiceRateLimiter The AdaptiveBucketRateLimiter service instance.
     */
    public AdaptiveBucketRateLimiterController(AdaptiveBucketServiceRateLimiter adaptiveBucketServiceRateLimiter) {
        this.adaptiveBucketServiceRateLimiter = adaptiveBucketServiceRateLimiter;
    }

    /**
     * Handles a request from a user and determines if it should be allowed based on the rate limit.
     *
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     * @throws IllegalArgumentException if the userId is null or empty.
     */
    public boolean handleRequest(String userId) {
        if ((userId == null) || (userId.isEmpty())) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        return adaptiveBucketServiceRateLimiter.validateRequest(userId);
    }

    /**
     * Updates the rate limit for a specific user.
     *
     * @param userId The user identifier.
     * @param newLimit The new rate limit to set for the user.
     * @throws IllegalArgumentException if the userId is null or empty.
     * @throws IllegalArgumentException if the user bucket does not exist.
     */
    public void updateUserLimit(String userId, long newLimit) {
        if ((userId == null) || (userId.isEmpty())) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        adaptiveBucketServiceRateLimiter.updateUserLimit(userId, newLimit);
    }

    /**
     * Resets the rate limit for a specific user.
     *
     * @param userId The user identifier.
     * @throws IllegalArgumentException if the userId is null or empty.
     * @throws IllegalArgumentException if the user bucket does not exist.
     */
    public void resetUserLimiter(String userId) {
        if ((userId == null) || (userId.isEmpty())) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        adaptiveBucketServiceRateLimiter.resetUserLimiter(userId);
    }
}