package titanVault.modelTest.smallDatasetTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import titanVault.model.TokenBucket;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketSmallTest {

    private TokenBucket tokenBucket;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        // Use a fixed clock for predictable results
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:00:00Z"), ZoneId.of("UTC"));
        // Initialize token bucket with a moderate capacity and refill period
        tokenBucket = new TokenBucket(100, 10, Duration.ofMinutes(1), fixedClock);
    }

    @Test
    public void testInitialTokenCapacity() {
        /* Positive Test Case: Ensure the bucket starts with full capacity. */
        assertEquals(100, tokenBucket.getToken(), "Initial token count should be 100.");
    }

    @Test
    public void testConsumeTokenWithinCapacity() {
        /* Positive Test Case: Consume tokens within the bucket's capacity. */
        assertTrue(tokenBucket.consume(), "Token should be successfully consumed within capacity.");
        assertEquals(99, tokenBucket.getToken(), "Token count should be 99 after consuming one token.");
    }

    @Test
    public void testConsumeTokenWhenEmpty() {
        /* Negative Test Case: Attempt to consume tokens when the bucket is empty. */
        // Empty the bucket
        for (int i = 0; i < 100; i++) {
            tokenBucket.consume();
        }
        assertFalse(tokenBucket.consume(), "Token should not be consumed when the bucket is empty.");
    }

    @Test
    public void testAddTokenWithinCapacity() {
        /* Positive Test Case: Add tokens and ensure the count does not exceed the capacity. */
        tokenBucket.addToken(20);
        assertEquals(100, tokenBucket.getToken(), "Token count should be capped at 100 after adding tokens.");
    }

    @Test
    public void testAddTokenExceedingCapacity() {
        /* Negative Test Case: Ensure adding tokens does not exceed the maximum capacity. */
        tokenBucket.addToken(50); // Add 50 tokens
        tokenBucket.addToken(60); // Attempt to add another 60 tokens
        assertEquals(100, tokenBucket.getToken(), "Token count should be capped at 100 after adding excess tokens.");
    }

    @Test
    public void testRemoveTokenSuccessfully() {
        /* Positive Test Case: Remove tokens and ensure it is done correctly. */
        tokenBucket.addToken(30);
        long removed = tokenBucket.removeToken(15);
        assertEquals(15, removed, "15 tokens should be removed successfully.");
        assertEquals(115, tokenBucket.getToken(), "Token count should be 115 after removing 15 tokens.");
    }

    @Test
    public void testRemoveMoreTokensThanAvailable() {
        /* Negative Test Case: Ensure removing more tokens than available does not result in negative values. */
        tokenBucket.addToken(20); // Add 20 tokens
        long removed = tokenBucket.removeToken(30); // Attempt to remove more than available
        assertEquals(20, removed, "The actual removed token count should be 20.");
        assertEquals(100, tokenBucket.getToken(), "Token count should remain at 100 after attempting to remove more than available.");
    }

    @Test
    public void testRefillTokensOverTime() {
        /* Positive Test Case: Ensure tokens are refilled over time. */
        // Simulate time passing
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:05:00Z"), ZoneId.of("UTC")); // 5 minutes later
        tokenBucket = new TokenBucket(100, 10, Duration.ofMinutes(1), fixedClock);
        assertEquals(100, tokenBucket.getToken(), "Token count should be 100 after refilling over time.");
    }

    @Test
    public void testSetTokenToSpecificValue() {
        /* Positive Test Case: Set the bucket's token count to a specific value and ensure it is correct. */
        tokenBucket.setToken(50);
        assertEquals(50, tokenBucket.getToken(), "Token count should be set to 50.");
    }

    @Test
    public void testRefillTokensCorrectly() {
        /* Positive Test Case: Ensure that refill logic works as expected. */
        tokenBucket.consume(); // Consume one token
        fixedClock = Clock.fixed(Instant.parse("2024-08-01T00:01:00Z"), ZoneId.of("UTC")); // Simulate 1 minute passing
        tokenBucket = new TokenBucket(100, 10, Duration.ofMinutes(1), fixedClock);
        assertEquals(99, tokenBucket.getToken(), "Token count should be 99 after consuming and refilling.");
    }

    @Test
    public void testTokenOverflowOnRefill() {
        /* Negative Test Case: Ensure token count does not exceed max capacity even after refill. */
        tokenBucket.addToken(200); // Add 200 tokens
        assertEquals(100, tokenBucket.getToken(), "Token count should be capped at 100 after adding excessive tokens.");
    }
}
