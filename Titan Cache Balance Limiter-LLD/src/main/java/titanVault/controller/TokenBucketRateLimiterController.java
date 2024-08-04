package titanVault.controller;

import titanVault.service.TokenBucketServiceRateLimiter;

public class TokenBucketRateLimiterController {

    private final TokenBucketServiceRateLimiter tokenBucketRateLimiter;

    public TokenBucketRateLimiterController(TokenBucketServiceRateLimiter tokenBucketRateLimiter) {
        this.tokenBucketRateLimiter = tokenBucketRateLimiter;
    }

    /**
     * Handles a request from a user and returns whether the request is allowed.
     *
     * @param userId The identifier of the user making the request.
     * @return true if the request is allowed, false otherwise.
     */

    public boolean handleRequest(String userId) {
        if ((userId == null) || (userId.isEmpty())) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        return tokenBucketRateLimiter.validateRequest(userId);
    }
}
