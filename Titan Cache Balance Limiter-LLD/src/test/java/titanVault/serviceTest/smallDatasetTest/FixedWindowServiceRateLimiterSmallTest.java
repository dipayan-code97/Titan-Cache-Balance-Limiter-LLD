package titanVault.serviceTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.service.FixedWindowServiceRateLimiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixedWindowServiceRateLimiterSmallTest {

    private FixedWindowServiceRateLimiter fixedWindowServiceRateLimiter;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize rate limiter with a max capacity of 100 requests per 1 minute window
        fixedWindowServiceRateLimiter = new FixedWindowServiceRateLimiter(100, Duration.ofMinutes(1), fixedClock);
    }

    @Test
    public void testAllowRequestWithinCapacity() {
        /* Positive Test Case: Allow requests within the window's capacity of 100. */
        for (int i = 0; i < 100; i++) {
            assertTrue(fixedWindowServiceRateLimiter.validateRequest("user1"), "Request should be allowed within the window's capacity.");
        }
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        /* Negative Test Case: Deny requests when the window's capacity of 100 is exceeded. */
        for (int i = 0; i < 100; i++) {
            fixedWindowServiceRateLimiter.validateRequest("user1"); // Fill the window capacity
        }
        assertFalse(fixedWindowServiceRateLimiter.validateRequest("user1"), "Request should be denied when the window's capacity is exceeded.");
    }

    @Test
    public void testNewWindowAfterInterval() {
        /* Positive Test Case: Allow requests in a new window after the previous window has passed. */
        for (int i = 0; i < 100; i++) {
            fixedWindowServiceRateLimiter.validateRequest("user2"); // Fill the window capacity
        }

        // Simulate the passage of time to move to the next window
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:00Z"), ZoneId.of("UTC")); // 1 minute later
        fixedWindowServiceRateLimiter = new FixedWindowServiceRateLimiter(100, Duration.ofMinutes(1), fixedClock);

        assertTrue(fixedWindowServiceRateLimiter.validateRequest("user2"), "Request should be allowed in the new window after the interval.");
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        /* Positive Test Case: Handle concurrent requests correctly within the same window. */
        Runnable task = () -> {
            for (int i = 0; i < 50; i++) {
                assertTrue(fixedWindowServiceRateLimiter.validateRequest("user3"), "Concurrent request should be allowed within the window's capacity.");
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
    public void testRequestsDeniedAfterMaxCapacityInWindow() {
        /* Negative Test Case: Requests should be denied once the max capacity of 100 is reached within the window. */
        for (int i = 0; i < 100; i++) {
            fixedWindowServiceRateLimiter.validateRequest("user4"); // Fill the window capacity
        }

        // Try one more request which should be denied
        assertFalse(fixedWindowServiceRateLimiter.validateRequest("user4"), "Request should be denied after reaching the max capacity in the window.");
    }

    @Test
    public void testRequestsAllowedAfterWindowReset() {
        /* Positive Test Case: Allow requests after the window has reset. */
        for (int i = 0; i < 100; i++) {
            fixedWindowServiceRateLimiter.validateRequest("user5"); // Fill the window capacity
        }

        // Simulate the passage of time to reset the window
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:02:00Z"), ZoneId.of("UTC")); // 2 minutes later
        fixedWindowServiceRateLimiter = new FixedWindowServiceRateLimiter(100, Duration.ofMinutes(1), fixedClock);

        assertTrue(fixedWindowServiceRateLimiter.validateRequest("user5"), "Request should be allowed after the window reset.");
    }

    @Test
    public void testInvalidUserId() {
        /* Negative Test Case: Handling of invalid user IDs. */
        assertFalse(fixedWindowServiceRateLimiter.validateRequest(""), "Request with an empty user ID should be denied.");
        assertFalse(fixedWindowServiceRateLimiter.validateRequest(null), "Request with a null user ID should be denied.");
    }
}
