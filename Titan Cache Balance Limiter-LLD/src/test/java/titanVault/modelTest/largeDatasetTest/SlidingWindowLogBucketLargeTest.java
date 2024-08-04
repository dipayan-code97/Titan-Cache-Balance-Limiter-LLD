package titanVault.modelTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import titanVault.model.SlidingWindowLogBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowLogBucketLargeTest {

    private SlidingWindowLogBucket slidingWindowLogBucket;
    private Clock fixedTimer;

    @BeforeEach
    void setUp() {
        // Initialize with a fixed clock for predictable results
        fixedTimer = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        slidingWindowLogBucket = new SlidingWindowLogBucket(10_000_000L, Duration.ofMinutes(1), fixedTimer);
    }

    @Test
    void testAllowRequestWithinCapacity() {
        assertTrue(slidingWindowLogBucket.allowRequest(), "Request should be allowed within capacity");
        assertEquals(1, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 1");
    }

    @Test
    void testAllowRequestExceedingCapacity() {
        for (int tester = 0; tester < 10_000_000L; tester++) {
            slidingWindowLogBucket.allowRequest();
        }
        assertFalse(slidingWindowLogBucket.allowRequest(), "Request should not be allowed when exceeding capacity");
        assertEquals(10_000_000L, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 10000000");
    }

    @Test
    void testAddTimestamp() {
        slidingWindowLogBucket.addTimestamp(fixedTimer.millis());
        assertEquals(1, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 1 after adding a timestamp");
        slidingWindowLogBucket.addTimestamp(fixedTimer.millis() + 1);
        assertEquals(2, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 2 after adding another timestamp");
    }

    @Test
    void testRemoveTimestamp() {
        long timestamp = fixedTimer.millis();
        slidingWindowLogBucket.addTimestamp(timestamp);
        assertTrue(slidingWindowLogBucket.removeTimestamp(timestamp), "Timestamp should be removed");
        assertEquals(0, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 0 after removal");
    }

    @Test
    void testSetTimestamp() {
        Deque<Long> timestamps = new ConcurrentLinkedDeque<>();
        for (int tester = 0; tester < 10_000_000L; tester++) {
            timestamps.add(fixedTimer.millis() + tester);
        }
        slidingWindowLogBucket.setTimestamp(timestamps);
        assertEquals(10_000_000L, slidingWindowLogBucket.getTimestamp(), "Timestamp count should be 10000000 after setting timestamps");
    }

    @Test
    @Timeout(5)
    void testConcurrency() throws InterruptedException {
        // This test case ensures that the SlidingWindowLogBucket works under concurrent access
        Runnable task = () -> {
            for (int tester = 0; tester < 10_00_000L; tester++) {
                slidingWindowLogBucket.allowRequest();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(slidingWindowLogBucket.getTimestamp() <= 10_000_000L, "Timestamp count should not exceed the maximum capacity");
    }
}
