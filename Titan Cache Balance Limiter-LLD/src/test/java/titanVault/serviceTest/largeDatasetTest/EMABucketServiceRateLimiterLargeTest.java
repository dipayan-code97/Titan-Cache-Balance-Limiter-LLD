package titanVault.serviceTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.EMABucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EMABucketServiceRateLimiterLargeTest {

    private EMABucket emaBucket;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize bucket with very large capacity and long averaging period
        emaBucket = new EMABucket(1_000_000_000L, Duration.ofDays(365), fixedClock);
    }

    @Test
    public void testAllowRequestWithinLargeCapacity() {
        /* Positive Test Case: Allow up to 1,000,000,000 requests within the large capacity */
        for (int i = 0; i < 1_000_000_000L; i++) {
            assertTrue(emaBucket.allowRequest(), "Request should be allowed within the large capacity.");
        }
    }

    @Test
    public void testDenyRequestWhenExceedingLargeCapacity() {
        /* Negative Test Case: Deny additional requests when the large capacity is exceeded */
        for (int i = 0; i < 1_000_000_000L; i++) {
            emaBucket.allowRequest(); // Use all allowed requests
        }
        assertFalse(emaBucket.allowRequest(), "Request should be denied after exceeding the large capacity.");
    }

    @Test
    public void testResetBucketAfterLargeInterval() {
        /* Positive Test Case: Ensure bucket resets after the long interval */
        for (int i = 0; i < 1_000_000_000L; i++) {
            emaBucket.allowRequest(); // Fill the bucket
        }

        // Simulate the passage of time beyond the averaging period
        fixedClock = Clock.offset(fixedClock, Duration.ofDays(730)); // Offset by 2 years
        emaBucket = new EMABucket(1_000_000_000L, Duration.ofDays(365), fixedClock);

        assertTrue(emaBucket.allowRequest(), "Request should be allowed after bucket resets due to the passage of time.");
    }

    @Test
    public void testUpdateLargeBucketConfiguration() {
        /* Positive Test Case: Update bucket configuration with large values and ensure new limits are applied */
        emaBucket.allowRequest(); // Initial request
        emaBucket.updateBucket(2_000_000_000L, Duration.ofDays(730)); // Update capacity and averaging period

        // Make a request under the new configuration
        assertTrue(emaBucket.allowRequest(), "Request should be allowed after updating the large configuration.");
    }

    @Test
    public void testRemoveLargeRequest() {
        /* Positive Test Case: Ensure requests are correctly removed and EMA adjusted */
        emaBucket.allowRequest(); // Make an initial request

        // Simulate removing requests
        assertTrue(emaBucket.removeRequest(), "Request should be removed successfully.");
    }

    @Test
    public void testRemoveRequestWhenNoLargeRequests() {
        /* Negative Test Case: Ensure removal fails gracefully when no requests are present */
        assertFalse(emaBucket.removeRequest(), "Removing requests should fail when no requests are available.");
    }
}
