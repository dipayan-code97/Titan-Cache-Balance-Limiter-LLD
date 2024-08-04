package titanVault.modelTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import titanVault.model.FixedWindowBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class FixedWindowBucketLargeTest {

    private FixedWindowBucket fixedWindowBucket;
    private Clock fixedTimer;

    @BeforeEach
    void setUp() {
        // Initialize with a fixed clock for predictable results
        fixedTimer = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        fixedWindowBucket = new FixedWindowBucket(10_000_000L, fixedTimer.millis(), 0, Duration.ofMinutes(1), fixedTimer);
    }

    @Test
    void testAllowRequestWithinWindow() {
        assertTrue(fixedWindowBucket.allowRequest(), "Request should be allowed within the window");
        assertEquals(1, fixedWindowBucket.getRequest(), "Request counter should be 1");
    }

    @Test
    void testAllowRequestExceedingCapacity() {
        for (long tester = 0; tester < 10_000_000L; tester++) {
            fixedWindowBucket.allowRequest();
        }
        assertFalse(fixedWindowBucket.allowRequest(), "Request should not be allowed when exceeding the capacity");
        assertEquals(10_000_000L, fixedWindowBucket.getRequest(), "Request counter should be 10000000L");
    }

    @Test
    void testAddRequest() {
        fixedWindowBucket.addRequest(5_000_000L);
        assertEquals(5_000_000L, fixedWindowBucket.getRequest(), "Request counter should be 5000000L after adding requests");
        fixedWindowBucket.addRequest(60_00_000L);
        assertEquals(10_000_000L, fixedWindowBucket.getRequest(), "Request counter should be capped at max capacity");
    }

    @Test
    void testRemoveRequest() {
        fixedWindowBucket.addRequest(10_000_000L);
        long removed = fixedWindowBucket.removeRequest(5_000_000L);
        assertEquals(5_000_000L, removed, "Should remove 5000000L requests");
        assertEquals(5_000_000L, fixedWindowBucket.getRequest(), "Request counter should be 5000000L after removal");
    }

    @Test
    void testRemoveMoreRequestsThanAvailable() {
        fixedWindowBucket.addRequest(10_000_000L);
        long removed = fixedWindowBucket.removeRequest(15_000_000L);
        assertEquals(10_000_000L, removed, "Should remove all 10000000L requests");
        assertEquals(0, fixedWindowBucket.getRequest(), "Request counter should be 0 after removal");
    }

    @Test
    void testWindowReset() {
        fixedWindowBucket.allowRequest(); // Should increment the request counter
        // Simulate window expiration by adjusting the clock
        fixedTimer = Clock.offset(fixedTimer, Duration.ofMinutes(2));
        fixedWindowBucket = new FixedWindowBucket(10_000_000L, fixedTimer.millis(), 0, Duration.ofMinutes(1), fixedTimer);
        assertEquals(0, fixedWindowBucket.getRequest(), "Request counter should reset after the window expires");
    }

    @Test
    void testSetRequest() {
        fixedWindowBucket.setRequest(7_000_000L);
        assertEquals(7_000_000L, fixedWindowBucket.getRequest(), "Request counter should be set to 7000000L");
        fixedWindowBucket.setRequest(12_000_000L); // Exceeding the capacity
        assertEquals(10_000_000L, fixedWindowBucket.getRequest(), "Request counter should be capped at max capacity");
    }

    @Test
    @Timeout(1)
    void testConcurrency() throws InterruptedException {
        // This test case ensures that the FixedWindowBucket works under concurrent access
        Runnable task = () -> {
            for (int worker = 0; worker < 5_000_000L; worker++) {
                fixedWindowBucket.allowRequest();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertTrue(fixedWindowBucket.getRequest() <= 10_000_000L, "Request counter should not exceed the maximum capacity");
    }
}
