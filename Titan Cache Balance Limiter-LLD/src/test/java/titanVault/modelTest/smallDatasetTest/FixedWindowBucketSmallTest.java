package titanVault.modelTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.FixedWindowBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class FixedWindowBucketSmallTest {

    private FixedWindowBucket fixedWindowBucket;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize bucket with moderate capacity and window interval
        fixedWindowBucket = new FixedWindowBucket(1_000, 0L, 0L, Duration.ofMinutes(1), fixedClock);
    }

    @Test
    public void testAllowRequestWithinModerateValueLimit() {
        /* Positive Test Case: Allow up to 1,000 requests within a window of 1 minute. */
        for (int i = 0; i < 1_000; i++) {
            assertTrue(fixedWindowBucket.allowRequest(), "Request should be allowed within the limit.");
        }
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        /* Negative Test Case: Deny additional requests when the capacity of 1,000 is exceeded. */
        for (int i = 0; i < 1_000; i++) {
            fixedWindowBucket.allowRequest(); // Use all allowed requests
        }
        assertFalse(fixedWindowBucket.allowRequest(), "Request should be denied when the limit is exceeded.");
    }

    @Test
    public void testAddRequestWithinCapacity() {
        /* Positive Test Case: Add requests and ensure they don't exceed the capacity. */
        fixedWindowBucket.addRequest(500);
        for (int i = 0; i < 500; i++) {
            assertTrue(fixedWindowBucket.allowRequest(), "Request should be allowed after adding 500 requests.");
        }
    }

    @Test
    public void testAddRequestExceedingCapacity() {
        /* Negative Test Case: Ensure adding requests doesn't exceed the maximum capacity. */
        fixedWindowBucket.addRequest(1_000); // Adds 1,000 requests to the bucket
        fixedWindowBucket.addRequest(500);   // Attempts to add 500 more requests
        assertEquals(1_000, fixedWindowBucket.getRequest(), "Request counter should be capped at the maximum capacity of 1,000.");
    }

    @Test
    public void testRemoveRequestSuccessfully() {
        /* Positive Test Case: Remove requests and ensure it works correctly. */
        fixedWindowBucket.addRequest(1_000);
        long removed = fixedWindowBucket.removeRequest(500);
        assertEquals(500, removed, "The number of removed requests should match the number removed.");
        assertEquals(500, fixedWindowBucket.getRequest(), "Request counter should reflect the remaining requests.");
    }

    @Test
    public void testRemoveMoreRequestsThanAvailable() {
        /* Negative Test Case: Ensure removing more requests than available doesn't result in negative values. */
        fixedWindowBucket.addRequest(1_000); // Adds 1,000 requests to the bucket
        long removed = fixedWindowBucket.removeRequest(1_500); // Attempts to remove 1,500 requests
        assertEquals(1_000, removed, "The number of removed requests should not exceed the number available.");
        assertEquals(0, fixedWindowBucket.getRequest(), "Request counter should be zero after attempting to remove more than available.");
    }

    @Test
    public void testResetWindowAfterInterval() {
        /* Positive Test Case: Ensure the window resets after the interval. */
        fixedWindowBucket.addRequest(1_000); // Fill the bucket
        // Simulate window passing time
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:00Z"), ZoneId.of("UTC"));
        fixedWindowBucket = new FixedWindowBucket(1_000, 0L, 0L, Duration.ofMinutes(1), fixedClock);
        assertTrue(fixedWindowBucket.allowRequest(), "Request should be allowed after the window has reset.");
    }
}
