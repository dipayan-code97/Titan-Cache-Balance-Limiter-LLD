package titanVault.serviceTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.service.LeakyBucketRateLimiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeakyBucketServiceRateLimiterSmallTest {

    private LeakyBucketRateLimiter rateLimiter;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize rate limiter with a bucket size of 100, leak rate of 1 request per second, and leak period of 1 second
        rateLimiter = new LeakyBucketRateLimiter(100, 1.0, Duration.ofSeconds(1), fixedClock);
    }

    @Test
    public void testAllowRequestWithinCapacity() {
        /* Positive Test Case: Allow requests within the bucket's capacity. */
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimiter.validateRequest("user1"), "Request should be allowed within the bucket's capacity.");
        }
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        /* Negative Test Case: Deny requests when the bucket's capacity is exceeded. */
        for (int i = 0; i < 100; i++) {
            rateLimiter.validateRequest("user1"); // Fill the bucket
        }
        assertFalse(rateLimiter.validateRequest("user1"), "Request should be denied when the bucket's capacity is exceeded.");
    }

    @Test
    public void testLeakageOverTime() {
        /* Positive Test Case: Allow requests after leakage period has passed. */
        for (int i = 0; i < 100; i++) {
            rateLimiter.validateRequest("user2"); // Fill the bucket
        }

        // Simulate the passage of time to allow the bucket to leak
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:00Z"), ZoneId.of("UTC")); // 1 minute later
        rateLimiter = new LeakyBucketRateLimiter(100, 1.0, Duration.ofSeconds(1), fixedClock);

        assertTrue(rateLimiter.validateRequest("user2"), "Request should be allowed after bucket leakage.");
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        /* Positive Test Case: Handle concurrent requests correctly. */
        Runnable task = () -> {
            for (int i = 0; i < 50; i++) {
                assertTrue(rateLimiter.validateRequest("user3"), "Concurrent request should be allowed.");
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
    public void testNoCapacityLeft() {
        /* Negative Test Case: Deny requests when no capacity is left. */
        LeakyBucketRateLimiter lowCapacityLimiter = new LeakyBucketRateLimiter(5, 1.0, Duration.ofSeconds(1), fixedClock);

        // Fill the bucket
        for (int i = 0; i < 5; i++) {
            lowCapacityLimiter.validateRequest("user4");
        }

        // Check if requests are denied when no capacity is left
        for (int i = 0; i < 5; i++) {
            assertFalse(lowCapacityLimiter.validateRequest("user4"), "Request should be denied when no capacity is left.");
        }
    }

    @Test
    public void testBucketRefillAfterExtendedTime() {
        /* Positive Test Case: Bucket should refill after an extended period. */
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(50, 0.5, Duration.ofSeconds(1), fixedClock);

        // Process requests to deplete the bucket
        for (int i = 0; i < 50; i++) {
            limiter.validateRequest("user5");
        }

        // Simulate the passage of time to exceed the leak period
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:02:00Z"), ZoneId.of("UTC")); // 2 minutes later
        limiter = new LeakyBucketRateLimiter(50, 0.5, Duration.ofSeconds(1), fixedClock);

        // Check if refill logic works after an extended period
        assertTrue(limiter.validateRequest("user5"), "Request should be allowed after extended time with refill.");
    }

    @Test
    public void testInvalidUserId() {
        /* Negative Test Case: Handling of invalid user IDs. */
        assertFalse(rateLimiter.validateRequest(""), "Request with an empty user ID should be denied.");
        assertFalse(rateLimiter.validateRequest(null), "Request with a null user ID should be denied.");
    }
}
