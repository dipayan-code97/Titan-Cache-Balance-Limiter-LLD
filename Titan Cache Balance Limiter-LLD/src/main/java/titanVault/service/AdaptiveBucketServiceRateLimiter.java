package titanVault.service;

import titanVault.model.AdaptiveBucket;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Implementation of the RateLimiter interface that validates requests based on the user identifier.
 */

public class AdaptiveBucketServiceRateLimiter implements RateLimiter {

    private final ConcurrentMap<String, AdaptiveBucket> adaptiveBuckets; // Thread-safe map to associate userId with an AdaptiveBucket
    private final long defaultLimit; // Default rate limit for users

    /*
     * Constructor to initialize the UserRateLimiter with a default rate limit.
     * @param defaultLimit The default limit for each user's bucket.
     */
    public AdaptiveBucketServiceRateLimiter(long defaultLimit) {
        this.adaptiveBuckets = new ConcurrentSkipListMap<>();
        this.defaultLimit = defaultLimit;
    }

    /*
     * Validate if a request from a user is allowed based on the rate limiting policy.
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    @Override
    public boolean validateRequest(String userId) {
        // Retrieve or create a bucket for the user
        AdaptiveBucket bucket = adaptiveBuckets.computeIfAbsent(userId, id -> new AdaptiveBucket(defaultLimit));
        // Validate the request based on the bucket's capacity
        return bucket.consumeRequest();
    }

    /*
     * Update the rate limit for a specific user.
     * @param userId The user identifier.
     * @param newLimit The new rate limit to set for the user.
     */
    public void updateUserLimit(String userId, long newLimit) {
        AdaptiveBucket bucket = adaptiveBuckets.get(userId);
        if (bucket != null) {
            bucket.adjustCountLimit(newLimit);
        } else {
            // Handle case where the user bucket does not exist
            throw new IllegalArgumentException("User bucket does not exist for userId: " + userId);
        }
    }

    /*
     * Reset the rate limit for a specific user.
     * @param userId The user identifier.
     */
    public void resetUserLimiter(String userId) {
        AdaptiveBucket bucket = adaptiveBuckets.get(userId);
        if (bucket != null) {
            bucket.reset();
        } else {
            // Handle case where the user bucket does not exist
            throw new IllegalArgumentException("User bucket does not exist for userId: " + userId);
        }
    }
}
