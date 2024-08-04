package titanVault.modelTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.LeakyBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class LeakyBucketSmallTest {

    private LeakyBucket leakyBucket;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize bucket with moderate capacity and leakage parameters
        leakyBucket = new LeakyBucket(1_000, 0L, 5.0, Duration.ofSeconds(10), fixedClock);
    }

    @Test
    public void testAllowRequestWithinModerateCapacity() {
        /* Positive Test Case: Allow requests within the bucket's capacity of 1,000 units. */
        for (int i = 0; i < 1_000; i++) {
            assertTrue(leakyBucket.allowRequest(), "Request should be allowed within the bucket's capacity.");
        }
    }

    @Test
    public void testDenyRequestWhenCapacityIsExceeded() {
        /* Negative Test Case: Deny requests when the bucket's capacity of 1,000 units is exceeded. */
        for (int i = 0; i < 1_000; i++) {
            leakyBucket.allowRequest(); // Fill the bucket
        }
        assertFalse(leakyBucket.allowRequest(), "Request should be denied when the bucket's capacity is exceeded.");
    }

    @Test
    public void testFillBucketWithinCapacity() {
        /* Positive Test Case: Fill the bucket and ensure the amount does not exceed capacity. */
        leakyBucket.fill(500);
        assertEquals(500, leakyBucket.getMeniscus(), "Bucket should contain 500 units after filling.");
    }

    @Test
    public void testFillBucketExceedingCapacity() {
        /* Negative Test Case: Ensure filling the bucket beyond capacity is capped. */
        leakyBucket.fill(1_500); // Attempt to fill more than the capacity
        assertEquals(1_000, leakyBucket.getMeniscus(), "Bucket should be capped at 1,000 units.");
    }

    @Test
    public void testDropBucketSuccessfully() {
        /* Positive Test Case: Drop water from the bucket and ensure it works correctly. */
        leakyBucket.fill(1_000);
        long dropped = leakyBucket.drop(500);
        assertEquals(500, dropped, "The amount dropped should match the amount specified.");
        assertEquals(500, leakyBucket.getMeniscus(), "Bucket should contain 500 units after dropping 500 units.");
    }

    @Test
    public void testDropMoreThanAvailable() {
        /* Negative Test Case: Ensure dropping more water than available does not result in negative values. */
        leakyBucket.fill(1_000); // Fill the bucket
        long dropped = leakyBucket.drop(1_500); // Attempt to drop more than available
        assertEquals(1_000, dropped, "The amount dropped should be capped at the available amount.");
        assertEquals(0, leakyBucket.getMeniscus(), "Bucket should be empty after attempting to drop more than available.");
    }

    @Test
    public void testLeakageOverTime() {
        /* Positive Test Case: Ensure the bucket leaks over time as expected. */
        leakyBucket.fill(1_000); // Fill the bucket
        // Simulate time passing for leakage
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:10:00Z"), ZoneId.of("UTC"));
        leakyBucket = new LeakyBucket(1_000, 1_000, 5.0, Duration.ofSeconds(10), fixedClock);
        // Expect some leakage over 10 minutes
        double expectedWaterLevel = Math.max(0, 1_000 - 5.0 * (600 / 10)); // 600 seconds / 10 seconds per period
        assertEquals(expectedWaterLevel, leakyBucket.getMeniscus(), "Bucket should show the correct level of water after leakage.");
    }

    @Test
    public void testSetBucketToSpecificValue() {
        /* Positive Test Case: Set the bucket's water level to a specific value and ensure it is correct. */
        leakyBucket.setBucket(500);
        assertEquals(500, leakyBucket.getMeniscus(), "Bucket should be set to 500 units.");
    }

    @Test
    public void testUpdateBucketCorrectly() {
        /* Positive Test Case: Update the bucket with additional water and ensure the amount does not exceed capacity. */
        leakyBucket.fill(500);
        leakyBucket.updateBucket(300);
        assertEquals(800, leakyBucket.getMeniscus(), "Bucket should contain 800 units after updating.");
    }
}
