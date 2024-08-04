package titanVault.modelTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.LeakyBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the LeakyBucket model.
 * This class contains various test cases to verify the correctness of LeakyBucket functionality.
 */
class LeakyBucketLargeTest {

    private LeakyBucket leakyBucket; // LeakyBucket instance for testing
    private Clock fixedClock; // Fixed clock to control the current time during tests

    /**
     * Initializes the LeakyBucket instance and sets up the fixed clock before each test.
     */
    @BeforeEach
    public void setUp() {
        // Set up a fixed clock to ensure predictable results in tests
        fixedClock = Clock.fixed(Instant.now(), java.time.ZoneId.systemDefault());
        // Initialize LeakyBucket with a max capacity of 100,000,000L, an initial water counter of 50,000,000L,
        // a water leak rate of 1.0, and a 1-minute leakage time period
        leakyBucket = new LeakyBucket(50_000_000L, 100_000_000L, 1.0, Duration.ofMinutes(1), fixedClock);
    }

    /**
     * Tests that the initial amount of water in the bucket is set correctly.
     */
    @Test
    public void testInitialWaterAmount() {
        // Verify that the initial water amount is as set
        assertEquals(50_000_000L, leakyBucket.getMeniscus());
    }

    /**
     * Tests allowing a request when the bucket has space and verifies the water amount increases.
     */
    @Test
    public void testAllowRequestWithSpace() {
        // Allow a request and verify water amount increases
        assertTrue(leakyBucket.allowRequest());
        assertEquals(50_000_001L, leakyBucket.getMeniscus());
    }

    /**
     * Tests allowing a request when the bucket is full, ensuring no more water can be added.
     */
    @Test
    public void testAllowRequestWhenFull() {
        // Fill the bucket to its maximum capacity
        leakyBucket.fill(50_000_000L);
        // Allow a request when the bucket is full
        assertFalse(leakyBucket.allowRequest());
        assertEquals(100_000_000L, leakyBucket.getMeniscus());
    }

    /**
     * Tests filling the bucket and ensures the amount does not exceed the maximum capacity.
     */
    @Test
    public void testFillBucket() {
        // Fill the bucket with a specific amount
        leakyBucket.fill(60_000_000L);
        assertEquals(100_000_000L, leakyBucket.getMeniscus()); // Should be capped at max capacity
    }

    /**
     * Tests dropping a specific amount of water from the bucket and verifies the remaining amount.
     */
    @Test
    public void testDropWater() {
        // Drop a specific amount of water and verify the remaining amount
        long dropped = leakyBucket.drop(10_000_000L);
        assertEquals(10_000_000L, dropped);
        assertEquals(50_000_000L - 10_000_000L, leakyBucket.getMeniscus());
    }

    /**
     * Tests dropping more water than available and verifies that only the available water is removed.
     */
    @Test
    public void testDropMoreWaterThanAvailable() {
        // Drop more water than available and verify only the available amount is removed
        long dropped = leakyBucket.drop(60_000_000L);
        assertEquals(50_000_000L, dropped); // Should remove all available water
        assertEquals(0L, leakyBucket.getMeniscus());
    }

    /**
     * Tests the leakage behavior over a period and ensures the water amount decreases correctly.
     */
    @Test
    public void testLeakageBehavior() {
        // Simulate the passage of time by offsetting the clock
        fixedClock = Clock.offset(fixedClock, Duration.ofMinutes(2));
        leakyBucket = new LeakyBucket(50_000_000L, 100_000_000L, 1.0, Duration.ofMinutes(1), fixedClock);

        // Simulate leakage and check the water amount
        leakyBucket.fill(0); // Trigger leakage
        assertTrue(leakyBucket.getMeniscus() < 50_000_000L); // Should have leaked some water
    }

    /**
     * Tests setting the water amount to a specific value and verifies the new amount.
     */
    @Test
    public void testSetBucket() {
        // Set the water amount to a specific value
        leakyBucket.setBucket(30_000_000L);
        assertEquals(30_000_000L, leakyBucket.getMeniscus());
    }

    /**
     * Tests setting the water amount beyond the maximum capacity and verifies it's capped at the maximum capacity.
     */
    @Test
    public void testSetBucketExceedingCapacity() {
        // Set the water amount to exceed the capacity and verify it's capped
        leakyBucket.setBucket(120_000_000L);
        assertEquals(100_000_000L, leakyBucket.getMeniscus()); // Should be capped at max capacity
    }

    /**
     * Tests updating the water amount by adding a specific value and verifies the new amount.
     */
    @Test
    public void testUpdateBucket() {
        // Update the water amount with a specific value
        leakyBucket.updateBucket(30_000_000L);
        assertEquals(50_000_000L + 30_000_000L, leakyBucket.getMeniscus()); // Should increase water amount
    }

    /**
     * Tests the behavior of filling and dropping water in a complex scenario.
     */
    @Test
    public void testComplexScenario() {
        // Fill the bucket and simulate leakage
        leakyBucket.fill(50_000_000L);
        fixedClock = Clock.offset(fixedClock, Duration.ofMinutes(2));
        leakyBucket = new LeakyBucket(50_000_000L, 100_000_000L, 1.0, Duration.ofMinutes(1), fixedClock);
        leakyBucket.fill(0); // Trigger leakage

        // Drop water and verify the remaining amount
        long dropped = leakyBucket.drop(20_000_000L);
        assertEquals(50_000_000L - 20_000_000L, leakyBucket.getMeniscus()); // Check the remaining water after drop
    }
}
