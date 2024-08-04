package titanVault.serviceTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.service.SlidingWindowLogRateLimiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlidingWindowLogServiceRateLimiterSmallTest {

    private SlidingWindowLogRateLimiter slidingWindowLogRateLimiter;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize rate limiter with a max capacity of 100 requests per sliding window interval of 1 minute
        slidingWindowLogRateLimiter = new SlidingWindowLogRateLimiter(100, Duration.ofMinutes(1), fixedClock);
    }

    @Test
    public void testAllowRequestWithinCapacity() {
        /* Positive Test Case: Allow requests within the window's capacity of 100. */
        for (int i = 0; i < 100; i++) {
            assertTrue(slidingWindowLogRateLimiter.validateRequest("user1"), "Request should be allowed within the window's capacity.");
        }
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        /* Negative Test Case: Deny requests when the window's capacity of 100 is exceeded. */
        for (int i = 0; i < 100; i++) {
            slidingWindowLogRateLimiter.validateRequest("user1"); // Fill the window capacity
        }
        assertFalse(slidingWindowLogRateLimiter.validateRequest("user1"), "Request should be denied when the window's capacity is exceeded.");
    }

    @Test
    public void testAllowRequestsAfterSlidingWindow() {
        /* Positive Test Case: Allow requests after the window has slid to a new interval. */
        for (int i = 0; i < 100; i++) {
            slidingWindowLogRateLimiter.validateRequest("user2"); // Fill the window capacity
        }

        // Simulate the passage of time to move to the next sliding window interval
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:00Z"), ZoneId.of("UTC")); // 1 minute later
        slidingWindowLogRateLimiter = new SlidingWindowLogRateLimiter(100, Duration.ofMinutes(1), fixedClock);

        assertTrue(slidingWindowLogRateLimiter.validateRequest("user2"), "Request should be allowed after the window has slid to a new interval.");
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        /* Positive Test Case: Handle concurrent requests correctly within the same sliding window. */
        Runnable task = () -> {
            for (int i = 0; i < 50; i++) {
                assertTrue(slidingWindowLogRateLimiter.validateRequest("user3"),
                        "Concurrent request should be allowed within the window's capacity.");
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
            slidingWindowLogRateLimiter.validateRequest("user4"); // Fill the window capacity
        }

        // Try one more request which should be denied
        assertFalse(slidingWindowLogRateLimiter.validateRequest("user4"),
                "Request should be denied after reaching the max capacity in the window.");
    }

    @Test
    public void testRequestsAllowedAfterWindowSlide() {
        /* Positive Test Case: Allow requests after the window has slid and old requests are phased out. */
        for (int i = 0; i < 100; i++) {
            slidingWindowLogRateLimiter.validateRequest("user5"); // Fill the window capacity
        }

        // Simulate the passage of time to slide the window and phase out old requests
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:02:00Z"), ZoneId.of("UTC")); // 2 minutes later
        slidingWindowLogRateLimiter = new SlidingWindowLogRateLimiter(100, Duration.ofMinutes(1), fixedClock);

        assertTrue(slidingWindowLogRateLimiter.validateRequest("user5"),
                "Request should be allowed after the sliding window interval.");
    }

    @Test
    public void testInvalidUserId() {
        /* Negative Test Case: Handling of invalid user IDs. */
        assertFalse(slidingWindowLogRateLimiter.validateRequest(""), "Request with an empty user ID should be denied.");
        assertFalse(slidingWindowLogRateLimiter.validateRequest(null), "Request with a null user ID should be denied.");
    }
}
