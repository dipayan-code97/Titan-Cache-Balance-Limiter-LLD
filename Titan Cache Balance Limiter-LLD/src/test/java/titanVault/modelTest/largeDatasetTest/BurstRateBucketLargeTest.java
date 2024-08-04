package titanVault.modelTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.BurstRateBucket;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BurstRateBucketLargeTest {

    private BurstRateBucket burstRateBucket;

    @BeforeEach
    public void setUp() {
        burstRateBucket = new BurstRateBucket(10, 100); // refillRate=10 tokens/sec, burstCapacity=100 tokens
    }

    @Test
    public void testValidateRequestWithInitialTokens() {
        // Test with initial tokens
        assertTrue(burstRateBucket.validateRequest("user1"));
        burstRateBucket.consumeToken("user1");
        assertFalse(burstRateBucket.validateRequest("user1"));
    }

    @Test
    public void testConsumeToken() {
        // Consume tokens until none are left
        for (int i = 0; i < 100; i++) {
            assertTrue(burstRateBucket.consumeToken("user1"));
        }
        assertFalse(burstRateBucket.consumeToken("user1")); // Should be false after 100 requests
    }

    @Test
    public void testTokenRefillAfterBurst() throws InterruptedException {
        BurstRateBucket bucket = new BurstRateBucket(10, 100); // refillRate=10 tokens/sec, burstCapacity=100 tokens

        // Consume all tokens in the bucket
        for (int i = 0; i < 100; i++) {
            bucket.consumeToken("user1");
        }
        assertFalse(bucket.validateRequest("user1")); // Should not have tokens left

        // Wait for a bit and check if tokens are refilled
        Thread.sleep(2000); // Wait for 2 seconds
        for (int i = 0; i < 10; i++) { // Refilling rate of 10 tokens/sec
            if (bucket.consumeToken("user1")) {
                assertTrue(bucket.validateRequest("user1"));
            }
        }
    }

    @Test
    public void testRetryAfterWithBurstSize() {
        BurstRateBucket bucket = new BurstRateBucket(10, 100); // refillRate=10 tokens/sec, burstCapacity=100 tokens
        for (int i = 0; i < 100; i++) {
            bucket.consumeToken("user1");
        }
        assertFalse(bucket.validateRequest("user1")); // Should be false after 100 requests
        long retryAfter = bucket.getRetryAfter("user1");
        assertTrue(retryAfter >= 1); // Retry after should be at least 1 second
    }


    @Test
    public void testConcurrentAccessWithHighConcurrency() throws InterruptedException {
        BurstRateBucket bucket = new BurstRateBucket(10, 100); // refillRate=10 tokens/sec, burstCapacity=100 tokens

        int numberOfThreads = 1000; // Adjusted number of threads to simulate high concurrency

        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    bucket.consumeToken("user1"); // Each thread makes a single request
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Wait for all threads to finish
        executor.shutdown();

        // Check final state after concurrent access
        assertTrue(bucket.validateRequest("user1"));
    }
}
