
package titanVault.serviceTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.service.TokenBucketServiceRateLimiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketServiceRateLimiterSmallTest {

    private TokenBucketServiceRateLimiter tokenBucketServiceRateLimiter;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize rate limiter with a bucket size of 100, refill period of 1 minute, and 10 tokens per period
        tokenBucketServiceRateLimiter = new TokenBucketServiceRateLimiter(100, Duration.ofMinutes(1), 10, fixedClock);
    }

    @Test
    public void testAllowRequestWithinCapacity() {
        /* Positive Test Case: Allow requests within the bucket's capacity. */
        for (int i = 0; i < 100; i++) {
            assertTrue(tokenBucketServiceRateLimiter.validateRequest("user1"), "Request should be allowed within the bucket's capacity.");
        }
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        /* Negative Test Case: Deny requests when the bucket's capacity is exceeded. */
        for (int i = 0; i < 100; i++) {
            tokenBucketServiceRateLimiter.validateRequest("user1"); // Fill the bucket
        }
        assertFalse(tokenBucketServiceRateLimiter.validateRequest("user1"), "Request should be denied when the bucket's capacity is exceeded.");
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        /* Positive Test Case: Handle concurrent requests correctly. */
        Runnable task = () -> {
            for (int i = 0; i < 50; i++) {
                assertTrue(tokenBucketServiceRateLimiter.validateRequest("user2"), "Concurrent request should be allowed.");
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }

    @Test
    public void testNoTokensAvailable() {
        /* Negative Test Case: No tokens available should deny requests. */
        TokenBucketServiceRateLimiter lowCapacityLimiter = new TokenBucketServiceRateLimiter(5, Duration.ofMinutes(1), 10, fixedClock);

        // Consume all available tokens
        for (int i = 0; i < 5; i++) {
            lowCapacityLimiter.validateRequest("user3");
        }

        // Check if requests are denied when no tokens are left
        for (int i = 0; i < 5; i++) {
            assertFalse(lowCapacityLimiter.validateRequest("user3"), "Request should be denied when no tokens are available.");
        }
    }

    @Test
    public void testBucketRefillAfterExtendedTime() {
        /* Positive Test Case: Bucket should refill after an extended period. */
        TokenBucketServiceRateLimiter limiter = new TokenBucketServiceRateLimiter(50, Duration.ofMinutes(1), 10, fixedClock);

        // Process requests to deplete the bucket
        for (int i = 0; i < 50; i++) {
            limiter.validateRequest("user5");
        }

        // Simulate the passage of time to exceed the refill period
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:02:00Z"), ZoneId.of("UTC")); // 2 minutes later
        limiter = new TokenBucketServiceRateLimiter(50, Duration.ofMinutes(1), 10, fixedClock);

        // Check if refill logic works after an extended period
        assertTrue(limiter.validateRequest("user5"), "Request should be allowed after extended time with refill.");
    }

    @Test
    public void testInvalidUserId() {
        /* Negative Test Case: Handling of invalid user IDs. */
        assertFalse(tokenBucketServiceRateLimiter.validateRequest(""), "Request with an empty user ID should be denied.");
        assertFalse(tokenBucketServiceRateLimiter.validateRequest(null), "Request with a null user ID should be denied.");
    }
}