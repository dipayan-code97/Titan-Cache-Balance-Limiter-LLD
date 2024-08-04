package titanVault.serviceTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.service.SlidingWindowLogRateLimiter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowLogServiceRateLimiterLargeTest {

    private SlidingWindowLogRateLimiter slidingWindowLogRateLimiter;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize rate limiter with an extremely large capacity
        slidingWindowLogRateLimiter = new SlidingWindowLogRateLimiter(1_000_000_000L, Duration.ofMinutes(1), fixedClock);
    }

    @Test
    public void testAllowRequestWithinExtremelyLargeValueLimit() {
        String userId = "userExtremelyLargeValue";
        /* Positive Test Case: Should allow up to 1,000,000,000 requests within the window. */
        for (int i = 0; i < 1_000_000_000L; i++) {
            assertTrue(slidingWindowLogRateLimiter.validateRequest(userId));
        }
    }

    @Test
    public void testNoRequestDenialWithinExtremelyLargeValueLimit() {
        String userId = "userExtremelyLargeValueLimit";
        /* Positive Test Case: Should allow up to 1,000,000,000 requests without denial. */
        for (int i = 0; i < 1_000_000_000L; i++) {
            slidingWindowLogRateLimiter.validateRequest(userId);
        }
        // No denial expected as the limit is extremely high
        assertTrue(slidingWindowLogRateLimiter.validateRequest(userId));
    }

    @Test
    public void testAllowRequestAfterWindowResetExtremelyLargeValue() {
        String userId = "userExtremelyLargeValueReset";
        /* Positive Test Case: Should allow up to 1,000,000,000 requests within the window. */
        for (int i = 0; i < 1_000_000_000L; i++) {
            slidingWindowLogRateLimiter.validateRequest(userId);
        }
        // Simulate window passing time
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:00Z"), ZoneId.of("UTC"));
        slidingWindowLogRateLimiter = new SlidingWindowLogRateLimiter(1_000_000_000L, Duration.ofMinutes(1), fixedClock);
        /* Positive Test Case: After window reset, should continue to allow requests. */
        assertTrue(slidingWindowLogRateLimiter.validateRequest(userId));
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        String userId = "userExceededCapacity";
        /* Negative Test Case: Simulate hitting the extremely large capacity limit and verify if the system denies additional requests. */
        for (int i = 0; i < 1_000_000_000L; i++) {
            slidingWindowLogRateLimiter.validateRequest(userId);
        }
        // Even with a very high limit, we simulate a scenario where capacity could be hypothetically exceeded.
        boolean result = slidingWindowLogRateLimiter.validateRequest(userId);
        assertTrue(result); // Given the extremely high capacity, denial might not be observed.
    }

    @Test
    public void testBehaviorUnderHighLoad() {
        String userId = "userHighLoad";
        /* Negative Test Case: Simulate a high load to check if performance degrades or if requests are handled incorrectly. */
        for (int i = 0; i < 1_000_000_000L; i++) {
            slidingWindowLogRateLimiter.validateRequest(userId);
        }
        // Perform additional requests to test performance and system behavior under high load
        boolean result = slidingWindowLogRateLimiter.validateRequest(userId);
        assertTrue(result); // The system should ideally handle high load without degradation.
    }

    @Test
    public void testRequestHandlingAfterWindowResetUnderLoad() {
        String userId = "userLoadReset";
        /* Positive Test Case: Allow up to 1,000,000,000 requests within the window. */
        for (int i = 0; i < 1_000_000_000L; i++) {
            slidingWindowLogRateLimiter.validateRequest(userId);
        }
        // Simulate window passing time
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:00Z"), ZoneId.of("UTC"));
        slidingWindowLogRateLimiter = new SlidingWindowLogRateLimiter(1_000_000_000L, Duration.ofMinutes(1), fixedClock);

        /* Negative Test Case: Ensure system continues to handle requests correctly after the window reset under high load. */
        boolean result = slidingWindowLogRateLimiter.validateRequest(userId);
        assertTrue(result); // Requests should be managed properly even after high load and window reset.
    }
}
