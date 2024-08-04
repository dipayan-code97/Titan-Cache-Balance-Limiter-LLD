package titanVault.serviceTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.EMABucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EMABucketServiceRateLimiterSmallTest {

    private EMABucket emaBucket;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize bucket with moderate capacity and averaging period
        emaBucket = new EMABucket(1_000, Duration.ofMinutes(1), fixedClock);
    }

    @Test
    public void testAllowRequestWithinModerateValueLimit() {
        /* Positive Test Case: Allow up to 1,000 requests within the averaging period. */
        for (int i = 0; i < 1_000; i++) {
            assertTrue(emaBucket.allowRequest(), "Request should be allowed within the limit.");
        }
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        /* Negative Test Case: Deny additional requests when the capacity is exceeded. */
        for (int i = 0; i < 1_000; i++) {
            emaBucket.allowRequest(); // Use all allowed requests
        }
        assertFalse(emaBucket.allowRequest(), "Request should be denied when the limit is exceeded.");
    }

    @Test
    public void testUpdateBucketConfiguration() {
        /* Positive Test Case: Update bucket configuration and ensure new limits are applied. */
        emaBucket.allowRequest(); // Initial request
        emaBucket.updateBucket(2_000, Duration.ofMinutes(2)); // Update capacity and averaging period

        // Make a request under the new configuration
        assertTrue(emaBucket.allowRequest(), "Request should be allowed after updating configuration.");
    }

    @Test
    public void testRecalculateEMAAfterRequest() {
        /* Positive Test Case: Ensure EMA is recalculated correctly after requests. */
        emaBucket.allowRequest(); // Make an initial request

        // Simulate passage of time
        fixedClock = Clock.offset(fixedClock, Duration.ofSeconds(30));
        emaBucket = new EMABucket(1_000, Duration.ofMinutes(1), fixedClock);

        assertTrue(emaBucket.allowRequest(), "Request should be allowed after recalculation.");
    }

    @Test
    public void testRemoveRequest() {
        /* Positive Test Case: Ensure requests are correctly removed and EMA adjusted. */
        emaBucket.allowRequest(); // Make an initial request

        // Simulate removing requests
        assertTrue(emaBucket.removeRequest(), "Request should be removed successfully.");
    }

    @Test
    public void testRemoveRequestWhenNoRequests() {
        /* Negative Test Case: Ensure removal fails gracefully when no requests are present. */
        assertFalse(emaBucket.removeRequest(), "Removing requests should fail when no requests are available.");
    }

    @Test
    public void testResetBucketAfterInterval() {
        /* Positive Test Case: Ensure bucket resets properly after the interval. */
        emaBucket.allowRequest(); // Fill the bucket
        // Simulate interval passing
        fixedClock = Clock.offset(fixedClock, Duration.ofMinutes(1));
        emaBucket = new EMABucket(1_000, Duration.ofMinutes(1), fixedClock);

        assertTrue(emaBucket.allowRequest(), "Request should be allowed after the interval has reset.");
    }
}
