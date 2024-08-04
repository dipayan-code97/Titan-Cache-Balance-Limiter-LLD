package titanVault.controller;

import titanVault.service.SlidingWindowCounterRateLimiter;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Controller to handle requests and interact with SlidingWindowCounterRateLimiter.
 */
public class SlidingWindowCounterRateLimiterController {

    private final SlidingWindowCounterRateLimiter rateLimiterService;
    private final ConcurrentMap<String, String> windowCounterBucket; // Example user mappings, can be replaced by an actual data source

    /**
     * Constructor for SlidingWindowCounterRateLimiterController.
     *
     * @param maxCapacity    Maximum number of requests allowed in the window.
     * @param windowSlideInterval  Duration of the sliding window.
     * @param currentTimer       Clock to get the current time.
     */
    public SlidingWindowCounterRateLimiterController(long maxCapacity,
                                                     Duration windowSlideInterval,
                                                     Clock currentTimer) {
        this.rateLimiterService = new SlidingWindowCounterRateLimiter(maxCapacity, windowSlideInterval, currentTimer);
        this.windowCounterBucket = new ConcurrentHashMap<>(); // Initialize user mappings
        initializeUserMappings(); // Populate user mappings
    }

    private void initializeUserMappings() {
        // Example user mappings, can be replaced by actual user data
        windowCounterBucket.put("user1", "User One");
        windowCounterBucket.put("user2", "User Two");
        windowCounterBucket.put("user3", "User Three");
    }

    /**
     * Handle a request for a specific user.
     *
     * @param userId The user identifier.
     * @return A message indicating if the request is allowed or not.
     */
    public String handleRequest(String userId) {
        if (rateLimiterService.validateRequest(userId)) {
            return "Request allowed for user: " + getUserDisplayName(userId);
        } else {
            return "Request denied for user: " + getUserDisplayName(userId);
        }
    }

    /**
     * Get the display name for a user based on the user ID.
     *
     * @param userId The user identifier.
     * @return The display name.
     */
    private String getUserDisplayName(String userId) {
        return windowCounterBucket.getOrDefault(userId, "Unknown User");
    }
}
