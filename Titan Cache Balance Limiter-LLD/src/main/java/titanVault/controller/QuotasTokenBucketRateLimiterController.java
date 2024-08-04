package titanVault.controller;

import titanVault.service.QuotasTokenBucketRateServiceLimiter;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class QuotasTokenBucketRateLimiterController {

    private final QuotasTokenBucketRateServiceLimiter rateLimiterService;
    private final ConcurrentMap<String, String> userMappings; // Example user mappings, can be replaced by an actual data source

    public QuotasTokenBucketRateLimiterController(long maxCapacity, Duration refillPeriod,
                                                  long tokensPerPeriod, Clock currentTimer,
                                                  long maxQuota, Duration quotaPeriod) {
        this.rateLimiterService = new QuotasTokenBucketRateServiceLimiter(
                maxCapacity, refillPeriod, tokensPerPeriod, currentTimer, maxQuota, quotaPeriod
        );
        this.userMappings = new ConcurrentSkipListMap<>(); // Initialize user mappings
        initializeUserMappings(); // Populate user mappings
    }

    private void initializeUserMappings() {
        // Example user mappings, can be replaced by actual user data
        userMappings.put("user1", "User One");
        userMappings.put("user2", "User Two");
        userMappings.put("user3", "User Three");
    }

    /**
     * Handle a request for a specific user.
     *
     * @param userId        The user identifier.
     * @param requestAmount The amount of resources requested.
     * @return A message indicating if the request is allowed or not.
     */
    public String handleRequest(String userId, long requestAmount) {
        if (rateLimiterService.validateRequest(userId, requestAmount)) {
            return "Request allowed for user: " + getUserDisplayName(userId);
        } else {
            return "Request denied for user: " + getUserDisplayName(userId);
        }
    }

    /**
     * Get the remaining quota for a specific user.
     *
     * @param userId The user identifier.
     * @return The remaining quota message.
     */
    public String getRemainingQuota(String userId) {
        long remainingQuota = rateLimiterService.getRemainingQuota(userId);
        return "Remaining quota for user " + getUserDisplayName(userId) + ": " + remainingQuota;
    }

    /**
     * Get the display name for a user based on the user ID.
     *
     * @param userId The user identifier.
     * @return The display name.
     */
    private String getUserDisplayName(String userId) {
        return userMappings.getOrDefault(userId, "Unknown User");
    }
}
