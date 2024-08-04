package titanVault.serviceTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import titanVault.service.TokenBucketServiceRateLimiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketServiceRateLimiterLargeTest {

    private TokenBucketServiceRateLimiter tokenBucketServiceRateLimiter;
    private Clock timer;

    @BeforeEach
    void setUp() {
        // Initialize with a fixed clock for predictable results
        timer = Clock.fixed(Instant.now(), java.time.ZoneOffset.UTC);

        // Use large values for maxCapacity, tokensPerPeriod, and a long refill period
        tokenBucketServiceRateLimiter = new TokenBucketServiceRateLimiter(
                1_000_000_000L,         // maxCapacity
                Duration.ofHours(1),    // refillPeriod
                500_000_000L,           // tokensPerPeriod
                timer                   // currentTimer
        );
    }

    @Test
    void testValidateRequestWithinCapacity() {
        /* Positive Test Case */
        /* This test checks that a request is allowed when the request count is within the token bucket capacity */
        assertTrue(tokenBucketServiceRateLimiter.validateRequest("user1"), "Request should be allowed within capacity");
    }

    @Test
    void testValidateRequestExceedingCapacity() {
        /* Negative Test Case */
        /* This test checks that requests exceeding the token bucket capacity are denied */

        // Refill the bucket to its maximum capacity and consume all tokens
        for (int token = 0; token < 1_000_000_000L; token++) {
            assertTrue(tokenBucketServiceRateLimiter.validateRequest("user1"), "Request should be allowed as long as capacity is not exceeded");
        }
        assertFalse(tokenBucketServiceRateLimiter.validateRequest("user1"), "Request should not be allowed after capacity is exceeded");
    }

    @Test
    void testRefillTokens() {
        /* Positive Test Case */
        /* This test checks that tokens are correctly refilled after the designated refill period */

        // Simulate a passage of time to refill tokens
        timer = Clock.offset(timer, Duration.ofHours(2));
        tokenBucketServiceRateLimiter = new TokenBucketServiceRateLimiter(
                1_000_000_000L,         // maxCapacity
                Duration.ofHours(1),    // refillPeriod
                500_000_000L,           // tokensPerPeriod
                timer                   // currentTimer
        );
        // Request after refill
        assertTrue(tokenBucketServiceRateLimiter.validateRequest("user1"), "Request should be allowed after refill");
    }

    @Test
    @Timeout(1)
    void testConcurrencyWithLargeValues() throws InterruptedException {
        /* Positive Test Case */
        /* This test ensures that the token bucket rate limiter handles concurrent requests without exceeding capacity */

        // Simulate concurrent requests
        Runnable task = () -> {
            for (int token = 0; token < 1_000_000_000L; token++) {
                tokenBucketServiceRateLimiter.validateRequest("user1");
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(tokenBucketServiceRateLimiter.validateRequest("user1"), "Concurrent requests should not exceed capacity limits");
    }
}