package titanVault.serviceTest.largeDatasetTest;

import org.junit.jupiter.api.Test;
import titanVault.model.AdaptiveBucket;

import static org.junit.jupiter.api.Assertions.*;

public class AdaptiveBucketRateServiceLimiterLargeTest {
    /*
     * Positive Test Case: Test if the AdaptiveBucket correctly handles a large dataset
     * and limits consumption according to the set limit.
     */
    @Test
    public void testLargeDatasetConsumption() {
        AdaptiveBucket bucket = new AdaptiveBucket(1_00_000_000L); // Large initial limit

        // Consume up to the limit
        for (long i = 0; i < 1_00_000_000L; i++) {
            assertTrue(bucket.consumeRequest(), "Bucket should allow consumption up to the limit.");
        }

        // Attempt to consume beyond the limit
        assertFalse(bucket.consumeRequest(), "Bucket should not allow consumption beyond the limit.");
    }

    /*
     * Negative Test Case: Test if the AdaptiveBucket handles negative values correctly.
     */
    @Test
    public void testNegativeAddAndRemove() {
        AdaptiveBucket bucket = new AdaptiveBucket(1_00_000_000);

        bucket.add(-50_000); // Adding a negative value
        assertEquals(0, bucket.get(), "Bucket count should not go below zero.");

        bucket.remove(-30_000); // Removing a negative value
        assertEquals(0, bucket.get(), "Bucket count should not increase by removing a negative value.");
    }

    /*
     * Positive Test Case: Test if the AdaptiveBucket correctly updates the limit
     * and handles a large dataset with the updated limit.
     */
    @Test
    public void testUpdateLimit() {
        AdaptiveBucket bucket = new AdaptiveBucket(500_000_000);

        // Add a large number of items and update limit
        bucket.add(400_000_000);
        bucket.update(300_000_000);
        assertEquals(300_000_000, bucket.get(), "Bucket count should update to the new value but not exceed the limit.");

        // Increase limit and test consumption
        bucket.adjustCountLimit(600_000_000);
        assertTrue(bucket.consumeRequest(), "Bucket should allow consumption after increasing the limit.");
    }

    /*
     * Negative Test Case: Test if the AdaptiveBucket handles limit updates properly
     * when the new limit is less than the current count.
     */
    @Test
    public void testLimitAdjustmentWhenCurrentCountExceedsNewLimit() {
        AdaptiveBucket bucket = new AdaptiveBucket(500_000_000);
        bucket.add(400_000_000);

        // Adjust limit to be less than current count
        bucket.adjustCountLimit(300_000_000);
        assertEquals(300_000_000, bucket.get(), "Bucket count should be adjusted to the new limit.");
    }
}
