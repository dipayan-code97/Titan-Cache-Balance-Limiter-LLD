package titanVault.controller;

import titanVault.service.LeakyBucketRateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class LeakyBucketRateLimiterController {

    private final Map<String, LeakyBucketRateLimiter> rateLimiters; // Stores rate limiters for different services

    public LeakyBucketRateLimiterController() {
        this.rateLimiters = new ConcurrentSkipListMap<>();
    }

    /**
     * Add a rate limiter for a specific service.
     * @param serviceId The identifier of the service.
     * @param rateLimiter The rate limiter to be added.
     */
    public void addRateLimiter(String serviceId, LeakyBucketRateLimiter rateLimiter) {
        rateLimiters.put(serviceId, rateLimiter);
    }

    /**
     * Handle a request for a specific service.
     * @param serviceId The identifier of the service.
     * @param userId The user identifier making the request.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean handleRequest(String serviceId, String userId) {
        LeakyBucketRateLimiter rateLimiter = rateLimiters.get(serviceId);
        if (rateLimiter == null) {
            throw new IllegalArgumentException("Service not found: " + serviceId);
        }
        return rateLimiter.validateRequest(userId);
    }

    /**
     * Get the status of a specific user's request counter for a service.
     * @param serviceId The identifier of the service.
     * @param userId The user identifier.
     * @return The current request count or water level in the leaky bucket.
     */
    public long getUserStatus(String serviceId, String userId) {
        LeakyBucketRateLimiter rateLimiter = rateLimiters.get(serviceId);
        if (rateLimiter == null) {
            throw new IllegalArgumentException("Service not found: " + serviceId);
        }
        return rateLimiter.getLeakyBucket(userId).getMeniscus();
    }
}