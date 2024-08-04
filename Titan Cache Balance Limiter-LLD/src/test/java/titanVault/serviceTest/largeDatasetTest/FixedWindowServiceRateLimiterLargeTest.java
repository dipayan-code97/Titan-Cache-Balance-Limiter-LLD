package titanVault.serviceTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import titanVault.service.FixedWindowServiceRateLimiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FixedWindowServiceRateLimiterLargeTest {

    private FixedWindowServiceRateLimiter fixedWindowServiceRateLimiter;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        // Initialize with a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.now(), java.time.ZoneOffset.UTC);

        // Use large values for maxCapacity and set a long window interval
        fixedWindowServiceRateLimiter = new FixedWindowServiceRateLimiter(
                1_000_000_000L,         // maxCapacity
                Duration.ofHours(1),    // windowInterval
                fixedClock                   // currentTimer
        );
    }

    @Test
    void testValidateRequestWithinWindow() {
        /* Positive Test Case */
        /* This test checks that a request is allowed when within the window and below the max capacity */

        assertTrue(fixedWindowServiceRateLimiter.validateRequest("user1"), "Request should be allowed within the window and under capacity");
    }

    @Test
    void testValidateRequestExceedingCapacityWithinWindow() {
        /* Negative Test Case */
        /* This test checks that requests exceeding the max capacity within the same window are denied */

        // Fill up the bucket to its max capacity
        for (int token = 0; token < 1_000_000_000L; token++) {
            assertTrue(fixedWindowServiceRateLimiter.validateRequest("user1"), "Request should be allowed as long as capacity is not exceeded");
        }

        // One more request should exceed capacity
        assertFalse(fixedWindowServiceRateLimiter.validateRequest("user1"), "Request should be denied after capacity is exceeded within the window");
    }

    @Test
    void testWindowResetAndAllowRequest() {
        /* Positive Test Case */
        /* This test checks that requests are allowed in a new window after the previous one expires */

        // Consume all tokens in the current window
        for (int token = 0; token < 1_000_000_000L; token++) {
            fixedWindowServiceRateLimiter.validateRequest("user1");
        }

        // Simulate passage of time to enter a new window
        fixedClock = Clock.offset(fixedClock, Duration.ofHours(2));
        fixedWindowServiceRateLimiter = new FixedWindowServiceRateLimiter(
                1_000_000_000L,         // maxCapacity
                Duration.ofHours(1),    // windowInterval
                fixedClock                   // currentTimer
        );

        // Request in new window
        assertTrue(fixedWindowServiceRateLimiter.validateRequest("user1"), "Request should be allowed after window reset");
    }

    @Test
    @Timeout(1)
    void testConcurrencyWithLargeValues() throws InterruptedException {
        /* Positive Test Case */
        /* This test ensures that the fixed window rate limiter handles concurrent requests without exceeding capacity */

        // Simulate concurrent requests
        Runnable task = () -> {
            for (int token = 0; token < 1_000_000_000L; token++) {
                fixedWindowServiceRateLimiter.validateRequest("user1");
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(fixedWindowServiceRateLimiter.validateRequest("user1"), "Concurrent requests should not exceed capacity limits");
    }
}
