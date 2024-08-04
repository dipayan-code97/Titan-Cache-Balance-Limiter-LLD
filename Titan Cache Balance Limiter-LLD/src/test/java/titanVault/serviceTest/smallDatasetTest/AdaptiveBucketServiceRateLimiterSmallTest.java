package titanVault.serviceTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.service.AdaptiveBucketServiceRateLimiter;
import static org.junit.jupiter.api.Assertions.*;

public class AdaptiveBucketServiceRateLimiterSmallTest {

    private AdaptiveBucketServiceRateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        // Initialize the rate limiter with a small default limit
        rateLimiter = new AdaptiveBucketServiceRateLimiter();
    }

    /*
     * Positive Test Case: Validate if a request is allowed when within the limit.
     * The bucket should allow requests up to the default limit.
     */
    @Test
    public void testRequestAllowedWithinLimit() {
        // Simulate requests within the limit
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimiter.validateRequest("user1"), "Request should be allowed within the limit.");
        }
    }

    /*
     * Negative Test Case: Validate if the rate limiter correctly rejects requests beyond the limit.
     * After reaching the limit, additional requests should be denied.
     */
    @Test
    public void testRequestRejectedBeyondLimit() {
        // Simulate requests up to the limit
        for (int i = 0; i < 100; i++) {
            rateLimiter.validateRequest("user1");
        }
        // One more request should be rejected as the limit is reached
        assertFalse(rateLimiter.validateRequest("user1"), "Request should be rejected beyond the limit.");
    }

    /*
     * Positive Test Case: Test updating the rate limit and allowing requests accordingly.
     * Update the limit and ensure the rate limiter respects the new limit.
     */
    @Test
    public void testUpdateRateLimit() {
        // Simulate requests up to the initial limit
        for (int i = 0; i < 100; i++) {
            rateLimiter.validateRequest("user1");
        }
        // Update the limit to a higher value
        rateLimiter.updateUserLimit("user1", 150);
        // Additional requests should be allowed with the new limit
        assertTrue(rateLimiter.validateRequest("user1"), "Request should be allowed after increasing the limit.");
    }

    /*
     * Negative Test Case: Test resetting the rate limiter and ensuring correct behavior.
     * After resetting, the rate limiter should reject requests if the limit is reached.
     */
    @Test
    public void testResetRateLimiter() {
        // Simulate requests up to the limit
        for (int i = 0; i < 100; i++) {
            rateLimiter.validateRequest("user1");
        }
        // Reset the rate limiter
        rateLimiter.resetUserLimiter("user1");
        // Request should be allowed after resetting as the count is reset
        assertTrue(rateLimiter.validateRequest("user1"), "Request should be allowed after resetting the rate limiter.");
    }
}
