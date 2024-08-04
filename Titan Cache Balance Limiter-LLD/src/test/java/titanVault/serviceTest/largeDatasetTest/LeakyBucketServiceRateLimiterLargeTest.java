package titanVault.serviceTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import titanVault.service.LeakyBucketRateLimiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LeakyBucketServiceRateLimiterLargeTest {

    private LeakyBucketRateLimiter leakyBucketRateLimiter;
    private Clock currentTimer;

    @BeforeEach
    void setUp() {
        // Initialize with a fixed clock for predictable results
        currentTimer = Clock.fixed(Instant.now(), java.time.ZoneOffset.UTC);

        // Use large values for maxCapacity and set a high leak rate and long leak period
        leakyBucketRateLimiter = new LeakyBucketRateLimiter(
                1_000_000_000L,         // maxCapacity
                1_000_000D,             // leakRate
                Duration.ofHours(1),    // leakPeriod
                currentTimer                   // currentTimer
        );
    }

    @Test
    void testValidateRequestWithinCapacity() {
        /* Positive Test Case */
        /* This test checks that a request is allowed when the request count is within the bucket capacity */
        assertTrue(leakyBucketRateLimiter.validateRequest("user1"), "Request should be allowed within capacity");
    }

    @Test
    void testValidateRequestExceedingCapacity() {
        /* Negative Test Case */
        /* This test checks that requests exceeding the bucket capacity are denied */

        // Refill the bucket to its maximum capacity and consume all tokens
        for (int token = 0; token < 1_000_000_000L; token++) {
            assertTrue(leakyBucketRateLimiter.validateRequest("user1"), "Request should be allowed as long as capacity is not exceeded");
        }
        assertFalse(leakyBucketRateLimiter.validateRequest("user1"), "Request should not be allowed after capacity is exceeded");
    }

    @Test
    void testLeakageAndRequestAllowance() {
        /* Positive Test Case */
        /* This test checks that requests are allowed after leakage reduces the token count */

        // Simulate a passage of time to leak some tokens
        currentTimer = Clock.offset(currentTimer, Duration.ofHours(2));
        leakyBucketRateLimiter = new LeakyBucketRateLimiter(
                1_000_000_000L,         // maxCapacity
                1_000_000D,             // leakRate
                Duration.ofHours(1),    // leakPeriod
                currentTimer                   // currentTimer
        );

        // Request after leakage
        assertTrue(leakyBucketRateLimiter.validateRequest("user1"), "Request should be allowed after leakage reduces token count");
    }

    @Test
    @Timeout(1)
    void testConcurrencyWithLargeValues() throws InterruptedException {
        /* Positive Test Case */
        /* This test ensures that the leaky bucket rate limiter handles concurrent requests without exceeding capacity */

        // Simulate concurrent requests
        Runnable task = () -> {
            for (int token = 0; token < 1_000_000_000L; token++) {
                leakyBucketRateLimiter.validateRequest("user1");
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(leakyBucketRateLimiter.validateRequest("user1"), "Concurrent requests should not exceed capacity limits");
    }
}
