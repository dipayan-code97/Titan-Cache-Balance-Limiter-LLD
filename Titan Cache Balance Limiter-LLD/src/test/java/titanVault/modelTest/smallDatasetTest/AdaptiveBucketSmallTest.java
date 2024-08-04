package titanVault.modelTest.smallDatasetTest;

import org.junit.jupiter.api.Test;
import titanVault.model.AdaptiveBucket;

import static org.junit.jupiter.api.Assertions.*;

public class AdaptiveBucketSmallTest {

    /*
     * Positive Test Case: Test basic functionality with a small dataset.
     */
    @Test
    public void testBasicFunctionality() {
        AdaptiveBucket bucket = new AdaptiveBucket(100); // Small initial limit

        assertTrue(bucket.consumeRequest(), "Bucket should allow consumption within limit.");
        assertEquals(1, bucket.get(), "Bucket count should be 1 after a successful consume.");

        bucket.add(50); // Adding a value
        assertEquals(51, bucket.get(), "Bucket count should be 51 after adding 50.");

        bucket.remove(20); // Removing a value
        assertEquals(31, bucket.get(), "Bucket count should be 31 after removing 20.");

        bucket.update(80); // Updating count
        assertEquals(80, bucket.get(), "Bucket count should be updated to 80.");

        bucket.reset(); // Resetting bucket
        assertEquals(0, bucket.get(), "Bucket count should be 0 after reset.");
    }

    /*
     * Negative Test Case: Test if the AdaptiveBucket handles invalid operations gracefully.
     */
    @Test
    public void testInvalidOperations() {
        AdaptiveBucket bucket = new AdaptiveBucket(100);

        bucket.add(200); // Adding more than the limit
        assertEquals(100, bucket.get(), "Bucket count should not exceed the limit.");

        bucket.remove(50); // Removing from an empty bucket
        assertEquals(100, bucket.get(), "Bucket count should not go below zero after removing.");

        bucket.update(-10); // Updating with a negative value
        assertEquals(0, bucket.get(), "Bucket count should not be negative after updating with a negative value.");
    }

    /*
     * Positive Test Case: Test if the AdaptiveBucket handles reset correctly.
     */
    @Test
    public void testResetFunctionality() {
        AdaptiveBucket bucket = new AdaptiveBucket(100);
        bucket.add(70);
        bucket.reset();
        assertEquals(0, bucket.get(), "Bucket count should be 0 after reset.");
    }

    /*
     * Negative Test Case: Test if the AdaptiveBucket handles adding/removing zero values.
     */
    @Test
    public void testAddRemoveZero() {
        AdaptiveBucket bucket = new AdaptiveBucket(100);

        bucket.add(0); // Adding zero should not change the count
        assertEquals(0, bucket.get(), "Bucket count should be 0 after adding zero.");

        bucket.remove(0); // Removing zero should not change the count
        assertEquals(0, bucket.get(), "Bucket count should be 0 after removing zero.");
    }
}