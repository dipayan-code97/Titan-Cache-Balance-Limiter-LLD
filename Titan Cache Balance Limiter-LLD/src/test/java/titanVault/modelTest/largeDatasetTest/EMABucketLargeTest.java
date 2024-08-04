package titanVault.modelTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import titanVault.model.EMABucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EMABucketLargeTest {

    private EMABucket emaBucket;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        // Initialize with a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.now(), java.time.ZoneOffset.UTC);

        // Use large values for maxCapacity and a long averaging period
        emaBucket = new EMABucket(
                1_000_000_000L,         // maxCapacity
                Duration.ofHours(1),    // averagingPeriod
                fixedClock              // currentTimer
        );
    }

    @Test
    void testAllowRequestWithinCapacity() {
        /* Positive Test Case */
        /* This test checks that a request is allowed when within the capacity and averaging period */
        assertTrue(emaBucket.allowRequest(), "Request should be allowed within the capacity and averaging period");
    }

    @Test
    void testAllowRequestExceedingCapacity() {
        /* Negative Test Case */
        /* This test checks that requests exceeding the max capacity are denied */

        // Simulate multiple requests to exceed the capacity
        for (int i = 0; i < 1_000_000_000; i++) {
            emaBucket.allowRequest();
        }

        // Simulate a request that should be denied due to capacity
        assertFalse(emaBucket.allowRequest(), "Request should be denied after exceeding capacity");
    }

    @Test
    void testEMARecalculationAfterConfigurationChange() {
        /* Positive Test Case */
        /* This test checks that the EMA recalculates correctly after changing the configuration */

        // Make some requests
        for (int i = 0; i < 500_000_000; i++) {
            emaBucket.allowRequest();
        }

        // Update the bucket configuration
        emaBucket.updateBucket(2_000_000_000L, Duration.ofHours(2));

        // Request in new configuration period
        assertTrue(emaBucket.allowRequest(), "Request should be allowed after configuration change");
    }

    @Test
    @Timeout(1)
    void testConcurrencyWithLargeValues() throws InterruptedException {
        /* Positive Test Case */
        /* This test ensures that the EMA bucket handles concurrent requests without exceeding capacity */

        // Simulate concurrent requests
        Runnable task = () -> {
            for (int i = 0; i < 1_000_000; i++) {
                emaBucket.allowRequest();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(emaBucket.allowRequest(), "Concurrent requests should not exceed capacity limits");
    }

    @Test
    void testRemoveRequest() {
        /* Positive Test Case */
        /* This test ensures that removing requests adjusts the EMA correctly */

        // Simulate adding requests
        for (int i = 0; i < 100_000; i++) {
            emaBucket.allowRequest();
        }

        // Remove some requests
        for (int i = 0; i < 50_000; i++) {
            emaBucket.removeRequest();
        }

        // Verify that requests are still allowed after removing some
        assertTrue(emaBucket.allowRequest(), "Requests should be allowed after removing some");
    }

    @Test
    void testUpdateBucketConfiguration() {
        /* Positive Test Case */
        /* This test checks that the bucket's configuration update is effective */

        // Make some requests
        for (int i = 0; i < 100_000; i++) {
            emaBucket.allowRequest();
        }

        // Update bucket configuration
        emaBucket.updateBucket(2_000_000_000L, Duration.ofHours(2));

        // Request in new configuration period
        assertTrue(emaBucket.allowRequest(), "Requests should be allowed after updating configuration");
    }
}
