package titanVault.modelTest.largeDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.model.TokenBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the TokenBucket model.
 * This class contains various test cases to verify the correctness of TokenBucket functionality.
 */
class TokenBucketLargeTest {

    private TokenBucket tokenBucket; // TokenBucket instance for testing
    private Clock fixedClock; // Fixed clock to control the current time during tests

    /**
     * Initializes the TokenBucket instance and sets up the fixed clock before each test.
     */
    @BeforeEach
    public void setUp() {
        // Set up a fixed clock to ensure predictable results in tests
        fixedClock = Clock.fixed(Instant.now(), java.time.ZoneId.systemDefault());
        // Initialize TokenBucket with a max capacity of 100,000,000, 5 tokens per period, and a 1-minute refill period
        tokenBucket = new TokenBucket(100_000_000L, 5, Duration.ofMinutes(1), fixedClock);
    }

    /**
     * Tests that the initial number of tokens is equal to the maximum capacity.
     */
    @Test
    public void testInitialTokenCount() {
        // Verify that the initial token count is equal to the maximum capacity
        assertEquals(100_000_000L, tokenBucket.getToken());
    }

    /**
     * Tests consuming a token from the bucket and verifies that the count decreases correctly.
     */
    @Test
    public void testConsumeToken() {
        // Consume one token and verify the count decreases
        assertTrue(tokenBucket.consume());
        assertEquals(100_000_000L - 1, tokenBucket.getToken());
    }

    /**
     * Tests consuming tokens when the bucket is empty, ensuring that no more tokens can be consumed.
     */
    @Test
    public void testConsumeTokenWhenEmpty() {
        // Consume all tokens to deplete the bucket
        for (int token = 0; token < 100_000_000; token++) {
            tokenBucket.consume();
        }
        // Attempt to consume more tokens should return false
        assertFalse(tokenBucket.consume());
    }

    /**
     * Tests adding tokens to the bucket and verifies that the count does not exceed the maximum capacity.
     */
    @Test
    public void testAddToken() {
        // Add tokens to the bucket and verify the count
        tokenBucket.addToken(1_000_000L);
        assertEquals(100_000_000L, tokenBucket.getToken()); // Should not exceed max capacity
    }

    /**
     * Tests removing a specific number of tokens from the bucket and verifies the remaining count.
     */
    @Test
    public void testRemoveToken() {
        // Remove a specific number of tokens and verify the count
        long removedTokens = tokenBucket.removeToken(3_000L);
        assertEquals(3_000L, removedTokens);
        assertEquals(100_000_000L - 3_000L, tokenBucket.getToken());
    }

    /**
     * Tests removing more tokens than available and verifies that only the available tokens are removed.
     */
    @Test
    public void testRemoveMoreTokensThanAvailable() {
        // Remove more tokens than available and verify only available tokens are removed
        tokenBucket.removeToken(100_000_001L); // Attempt to remove more than available
        assertEquals(0L, tokenBucket.getToken()); // Should remove all available tokens
    }

    /**
     * Tests refilling the bucket after a certain period and ensures it fills up to the maximum capacity.
     */
    @Test
    public void testRefillAfterPeriod() {
        // Simulate the passage of time by adjusting the fixed clock
        fixedClock = Clock.offset(fixedClock, Duration.ofMinutes(2));
        tokenBucket = new TokenBucket(100_000_000L, 5, Duration.ofMinutes(1), fixedClock);

        // Initially should have 100,000,000 tokens
        tokenBucket.refill(); // Refilling should not be needed as time has not passed

        // Simulate time passing and check token count after refilling
        tokenBucket.addToken(0); // Force refill
        assertEquals(100_000_000L, tokenBucket.getToken()); // Should be full capacity
    }

    /**
     * Tests setting the token count to a specific value and verifies the new count.
     */
    @Test
    public void testSetToken() {
        // Set the token count to a specific value
        tokenBucket.setToken(50_000_000L);
        assertEquals(50_000_000L, tokenBucket.getToken());
    }

    /**
     * Tests setting the token count beyond the maximum capacity and verifies it's capped at the maximum capacity.
     */
    @Test
    public void testSetTokenExceedingCapacity() {
        // Set the token count to exceed the capacity and verify it's capped
        tokenBucket.setToken(150_000_000L);
        assertEquals(100_000_000L, tokenBucket.getToken()); // Should be capped at max capacity
    }

    /**
     * Tests that tokens are refilled correctly when a period elapses and verifies the count is updated properly.
     */
    @Test
    public void testRefillBehavior() {
        // Simulate passage of time by offsetting the clock
        fixedClock = Clock.offset(fixedClock, Duration.ofMinutes(1));
        tokenBucket = new TokenBucket(100_000_000L, 5, Duration.ofMinutes(1), fixedClock);
        tokenBucket.refill(); // Should refill with 5 tokens after one period

        assertEquals(100_000_000L, tokenBucket.getToken()); // Should be full capacity
    }

    /**
     * Tests token consumption and refill behavior when a period elapses with continuous consumption.
     */
    @Test
    public void testConsumeAndRefill() {
        // Consume 50 tokens
        for (int token = 0; token < 50; token++) {
            tokenBucket.consume();
        }
        // Simulate passage of time
        fixedClock = Clock.offset(fixedClock, Duration.ofMinutes(1));
        tokenBucket.refill(); // Refill should add tokens

        assertTrue(tokenBucket.getToken() > 100_000_000L - 50); // There should be tokens available
    }

    /**
     * Tests the behavior when adding tokens after depleting the bucket.
     */
    @Test
    public void testAddTokenAfterDepletion() {
        // Deplete the bucket
        for (long token = 0; token < 100_000_000L; token++) {
            tokenBucket.consume();
        }
        // Add tokens and verify the count
        tokenBucket.addToken(7_000_000L);
        assertEquals(100_000_000L, tokenBucket.getToken()); // Should not exceed max capacity
    }

    /**
     * Tests the token count after a series of refills and token removals.
     */
    @Test
    public void testComplexScenario() {
        // Consume 30,000 tokens
        for (long token = 0; token < 30_000L; token++) {
            tokenBucket.consume();
        }
        // Simulate time passing for one period
        fixedClock = Clock.offset(fixedClock, Duration.ofMinutes(1));
        tokenBucket.refill(); // Refill should occur

        // Verify token count after refill
        assertEquals(100_000_000L, tokenBucket.getToken()); // Should be full capacity

        // Remove 40,000 tokens and verify count
        tokenBucket.removeToken(40_000L);
        assertEquals(100_000_000L - 40_000L, tokenBucket.getToken()); // Should have 60,000,000 tokens remaining
    }
}
