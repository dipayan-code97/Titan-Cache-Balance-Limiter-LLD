package titanVault.modelTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.SlidingWindowCounterBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowCounterBucketLargeTest {

    private SlidingWindowCounterBucket slidingWindowCounterBucket;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        slidingWindowCounterBucket = new SlidingWindowCounterBucket(
                1_00_000_000L, // Max capacity
                Duration.ofMinutes(1), // Sliding window interval
                fixedClock,
                fixedClock.millis(),
                0 // Initial request counter
        );
    }

    @Test
    public void testAllowRequestWithinWindow() {
        // Test allowing a request when the bucket is empty
        assertTrue(slidingWindowCounterBucket.allowRequest());
        assertEquals(1, slidingWindowCounterBucket.getRequestCounter());
    }

    @Test
    public void testAllowRequestWhenFull() {
        // Fill the bucket to max capacity
        for (int token = 0; token < 1_00_000_000L; token++) {
            slidingWindowCounterBucket.allowRequest();
        }
        // Try allowing another request should fail as the bucket is full
        assertFalse(slidingWindowCounterBucket.allowRequest());
    }

    @Test
    public void testAddRequestCount() {
        // Add requests and verify the count
        slidingWindowCounterBucket.addRequestCount(5000);
        assertEquals(5000, slidingWindowCounterBucket.getRequestCounter());
    }

    @Test
    public void testRemoveRequestCount() {
        // Add some requests first
        slidingWindowCounterBucket.addRequestCount(5000);
        // Remove requests and verify the remaining count
        assertTrue(slidingWindowCounterBucket.removeRequestCount(2000));
        assertEquals(3000, slidingWindowCounterBucket.getRequestCounter());
    }

    @Test
    public void testRemoveMoreRequestsThanAvailable() {
        // Add some requests first
        slidingWindowCounterBucket.addRequestCount(3000);
        // Try to remove more requests than available
        assertFalse(slidingWindowCounterBucket.removeRequestCount(4000));
        assertEquals(3000, slidingWindowCounterBucket.getRequestCounter());
    }

    @Test
    public void testGetRequestCounterAfterSlidingWindow() {
        // Add some requests
        slidingWindowCounterBucket.addRequestCount(5000);
        // Simulate passage of time
        fixedClock = Clock.offset(fixedClock, Duration.ofMinutes(2));
        slidingWindowCounterBucket = new SlidingWindowCounterBucket(
                1_00_000_000L,
                Duration.ofMinutes(1),
                fixedClock,
                fixedClock.millis(),
                5000
        );
        // Ensure the request counter is reset after sliding window
        assertEquals(0, slidingWindowCounterBucket.getRequestCounter());
    }

    @Test
    public void testSetRequestCounter() {
        // Add some requests first
        slidingWindowCounterBucket.addRequestCount(3000);
        // Set the request counter to a specific value
        slidingWindowCounterBucket.setRequestCounter(1000);
        assertEquals(1000, slidingWindowCounterBucket.getRequestCounter());
    }

    @Test
    public void testSetRequestCounterExceedingCapacity() {
        // Set the request counter beyond the max capacity and verify it's capped
        slidingWindowCounterBucket.setRequestCounter(1_00_000_001L); // Beyond capacity
        assertEquals(1_00_000_000L, slidingWindowCounterBucket.getRequestCounter()); // Should be capped
    }

    @Test
    public void testSlidingWindowBehavior() {
        // Add requests and simulate the passage of time
        slidingWindowCounterBucket.addRequestCount(5000);
        fixedClock = Clock.offset(fixedClock, Duration.ofMinutes(1)); // Simulate sliding window
        slidingWindowCounterBucket = new SlidingWindowCounterBucket(
                1_00_000_000L,
                Duration.ofMinutes(1),
                fixedClock,
                fixedClock.millis(),
                5000
        );
        // Add more requests after sliding window
        slidingWindowCounterBucket.addRequestCount(1000);
        assertEquals(1000, slidingWindowCounterBucket.getRequestCounter());
    }
}
