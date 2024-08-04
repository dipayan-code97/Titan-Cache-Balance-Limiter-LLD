package titanVault.modelTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.SlidingWindowLogBucket;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.Clock;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowLogBucketSmallTest {

    private SlidingWindowLogBucket slidingWindowLogBucket;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize bucket with a moderate capacity and sliding window interval
        slidingWindowLogBucket = new SlidingWindowLogBucket(100, Duration.ofMinutes(1), fixedClock);
    }

    @Test
    public void testAllowRequestWithinCapacity() {
        /* Positive Test Case: Allow requests within the bucket's capacity of 100 units. */
        for (int i = 0; i < 100; i++) {
            assertTrue(slidingWindowLogBucket.allowRequest(), "Request should be allowed within the bucket's capacity.");
        }
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        /* Negative Test Case: Deny requests when the bucket's capacity of 100 units is exceeded. */
        for (int i = 0; i < 100; i++) {
            slidingWindowLogBucket.allowRequest(); // Fill the bucket
        }
        assertFalse(slidingWindowLogBucket.allowRequest(), "Request should be denied when the bucket's capacity is exceeded.");
    }

    @Test
    public void testAddTimestampWithinCapacity() {
        /* Positive Test Case: Add timestamps and ensure the log does not exceed the capacity. */
        slidingWindowLogBucket.addTimestamp(fixedClock.millis());
        slidingWindowLogBucket.addTimestamp(fixedClock.millis() + 1_000L); // 1 second later
        assertEquals(2, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 2 after adding two timestamps.");
    }

    @Test
    public void testAddTimestampExceedingCapacity() {
        /* Negative Test Case: Ensure adding timestamps does not exceed the maximum capacity. */
        for (int i = 0; i < 101; i++) { // Exceed the capacity of 100
            slidingWindowLogBucket.addTimestamp(fixedClock.millis() + i * 1_000L); // Add timestamps
        }
        assertEquals(100, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be capped at 100.");
    }

    @Test
    public void testRemoveTimestampSuccessfully() {
        /* Positive Test Case: Remove a specific timestamp and ensure it is removed correctly. */
        long timestamp1 = fixedClock.millis();
        long timestamp2 = fixedClock.millis() + 1_000L;
        slidingWindowLogBucket.addTimestamp(timestamp1);
        slidingWindowLogBucket.addTimestamp(timestamp2);
        assertTrue(slidingWindowLogBucket.removeTimestamp(timestamp1), "Timestamp should be removed successfully.");
        assertEquals(1, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 1 after removing one timestamp.");
    }

    @Test
    public void testRemoveTimestampNotFound() {
        /* Negative Test Case: Ensure removing a non-existent timestamp does not alter the log. */
        long timestamp = fixedClock.millis();
        slidingWindowLogBucket.addTimestamp(timestamp);
        assertFalse(slidingWindowLogBucket.removeTimestamp(timestamp + 1_000L), "Removing a non-existent timestamp should fail.");
        assertEquals(1, slidingWindowLogBucket.getTimestamp(), "Timestamp count should remain 1.");
    }

    @Test
    public void testGetTimestampCount() {
        /* Positive Test Case: Ensure getting the timestamp count returns the correct number. */
        slidingWindowLogBucket.addTimestamp(fixedClock.millis());
        slidingWindowLogBucket.addTimestamp(fixedClock.millis() + 1_000L);
        assertEquals(2, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 2.");
    }

    @Test
    public void testSetTimestamp() {
        /* Positive Test Case: Set the log to a specific set of timestamps and ensure it is updated correctly. */
        Deque<Long> timestamps = new ConcurrentLinkedDeque<>();
        long timestamp1 = fixedClock.millis();
        long timestamp2 = fixedClock.millis() + 1_000L;
        timestamps.add(timestamp1);
        timestamps.add(timestamp2);
        slidingWindowLogBucket.setTimestamp(timestamps);
        assertEquals(2, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 2 after setting new timestamps.");
    }

    @Test
    public void testCleanOldTimestamps() {
        /* Positive Test Case: Ensure old timestamps outside the window are cleaned up correctly. */
        slidingWindowLogBucket.addTimestamp(fixedClock.millis() - Duration.ofMinutes(2).toMillis()); // Timestamp outside the window
        slidingWindowLogBucket.addTimestamp(fixedClock.millis()); // Current timestamp
        // Simulate time passing
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:00Z"), ZoneId.of("UTC")); // 1 minute later
        slidingWindowLogBucket = new SlidingWindowLogBucket(100, Duration.ofMinutes(1), fixedClock);
        assertEquals(1, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 1 after cleaning old timestamps.");
    }
}
