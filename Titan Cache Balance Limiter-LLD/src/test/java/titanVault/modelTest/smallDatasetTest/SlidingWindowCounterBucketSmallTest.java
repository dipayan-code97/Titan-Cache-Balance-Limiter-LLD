package titanVault.modelTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.SlidingWindowCounterBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowCounterBucketSmallTest {

    private SlidingWindowCounterBucket slidingWindowCounterBucket;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize bucket with moderate capacity and sliding window interval
        slidingWindowCounterBucket = new SlidingWindowCounterBucket(500, Duration.ofMinutes(1), fixedClock, 0L, 0L);
    }

    @Test
    public void testAllowRequestWithinCapacity() {
        /* Positive Test Case: Allow requests within the bucket's capacity of 500 units. */
        for (int i = 0; i < 500; i++) {
            assertTrue(slidingWindowCounterBucket.allowRequest(), "Request should be allowed within the bucket's capacity.");
        }
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        /* Negative Test Case: Deny requests when the bucket's capacity of 500 units is exceeded. */
        for (int i = 0; i < 500; i++) {
            slidingWindowCounterBucket.allowRequest(); // Fill the bucket
        }
        assertFalse(slidingWindowCounterBucket.allowRequest(), "Request should be denied when the bucket's capacity is exceeded.");
    }

    @Test
    public void testAddRequestCountWithinCapacity() {
        /* Positive Test Case: Add requests and ensure the count does not exceed the capacity. */
        slidingWindowCounterBucket.addRequestCount(300);
        assertTrue(slidingWindowCounterBucket.allowRequest(), "Request should be allowed after adding 300 requests.");
    }

    @Test
    public void testAddRequestCountExceedingCapacity() {
        /* Negative Test Case: Ensure adding requests doesn't exceed the maximum capacity. */
        slidingWindowCounterBucket.addRequestCount(500); // Adds 500 requests
        slidingWindowCounterBucket.addRequestCount(300); // Attempts to add another 300 requests
        assertEquals(500, slidingWindowCounterBucket.getRequestCounter(), "Request count should be capped at 500.");
    }

    @Test
    public void testRemoveRequestCountSuccessfully() {
        /* Positive Test Case: Remove requests and ensure it works correctly. */
        slidingWindowCounterBucket.addRequestCount(500);
        boolean removed = slidingWindowCounterBucket.removeRequestCount(200);
        assertTrue(removed, "The request count should have been removed successfully.");
        assertEquals(300, slidingWindowCounterBucket.getRequestCounter(), "Request counter should be 300 after removing 200 requests.");
    }

    @Test
    public void testRemoveMoreRequestsThanAvailable() {
        /* Negative Test Case: Ensure removing more requests than available does not result in negative values. */
        slidingWindowCounterBucket.addRequestCount(500); // Add 500 requests
        boolean removed = slidingWindowCounterBucket.removeRequestCount(600); // Attempt to remove more than available
        assertFalse(removed, "The removal operation should fail when trying to remove more than available.");
        assertEquals(500, slidingWindowCounterBucket.getRequestCounter(), "Request counter should remain at 500 after attempting to remove more than available.");
    }

    @Test
    public void testResetWindowAfterInterval() {
        /* Positive Test Case: Ensure the window resets after the specified interval. */
        slidingWindowCounterBucket.addRequestCount(500);
        // Simulate time passing for window slide
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:00Z"), ZoneId.of("UTC"));
        slidingWindowCounterBucket = new SlidingWindowCounterBucket(500, Duration.ofMinutes(1), fixedClock, 0L, 500L);
        assertTrue(slidingWindowCounterBucket.allowRequest(), "Request should be allowed after the window has reset.");
    }

    @Test
    public void testSetRequestCounterToSpecificValue() {
        /* Positive Test Case: Set the bucket's request counter to a specific value and ensure it is correct. */
        slidingWindowCounterBucket.addRequestCount(300);
        slidingWindowCounterBucket.setRequestCounter(100);
        assertEquals(100, slidingWindowCounterBucket.getRequestCounter(), "Request counter should be set to 100.");
    }

    @Test
    public void testSlidingWindowAdjustment() {
        /* Positive Test Case: Ensure sliding window adjustments are handled correctly. */
        slidingWindowCounterBucket.addRequestCount(200);
        // Simulate time passing to slide the window
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:30Z"), ZoneId.of("UTC")); // 30 seconds later
        slidingWindowCounterBucket = new SlidingWindowCounterBucket(500, Duration.ofMinutes(1), fixedClock, 0L, 200L);
        assertEquals(200, slidingWindowCounterBucket.getRequestCounter(),
                "Request counter should reflect the correct value after sliding the window.");
    }
}
