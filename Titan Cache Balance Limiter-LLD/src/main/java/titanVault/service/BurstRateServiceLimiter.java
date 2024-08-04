package titanVault.service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * BurstRateLimiter implements a rate limiter with a burst capacity.
 * It allows a burst of requests up to a specified capacity, and then enforces
 * a rate limit based on the refill rate.
 */
public class BurstRateServiceLimiter implements RateLimiter, BurstRateLimiter {

    private final long refillRate; // Number of tokens added per second
    private final long burstCapacity; // Maximum number of tokens that the bucket can hold
    private final AtomicLong currentTokens; // Current number of tokens in the bucket
    private final ReentrantReadWriteLock locker; // Lock to ensure thread safety

    /**
     * Constructor to initialize the BurstRateLimiter.
     *
     * @param refillRate The rate at which tokens are added to the bucket (tokens per second).
     * @param burstCapacity The maximum number of tokens that the bucket can hold.
     */
    public BurstRateServiceLimiter(long refillRate, long burstCapacity) {
        this.refillRate = refillRate;
        this.burstCapacity = burstCapacity;
        this.currentTokens = new AtomicLong(burstCapacity);
        this.locker = new ReentrantReadWriteLock();
        startRefillThread(); // Start the token refill thread
    }

    /**
     * Validates if a request is allowed based on the rate limiting policy.
     *
     * @param userId The user identifier (not used in this implementation).
     * @return true if the request is allowed, false otherwise.
     */
    @Override
    public boolean validateRequest(String userId) {
        locker.readLock().lock(); // Acquire read lock
        try {
            return currentTokens.get() > 0; // Check if there are tokens available
        } finally {
            locker.readLock().unlock(); // Release read lock
        }
    }

    /**
     * Deducts a token for a request if allowed.
     *
     * @param userId The user identifier (not used in this implementation).
     * @return true if the request was allowed and a token was deducted, false otherwise.
     */
    public boolean consumeToken(String userId) {
        locker.writeLock().lock(); // Acquire write lock
        try {
            if (currentTokens.get() > 0) {
                currentTokens.decrementAndGet(); // Deduct a token
                return true;
            } else {
                return false; // No tokens available
            }
        } finally {
            locker.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Starts a thread that refills the bucket with tokens at the specified rate.
     */
    private void startRefillThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // Wait for 1 second
                    locker.writeLock().lock(); // Acquire write lock
                    try {
                        long newTokens = Math.min(burstCapacity, currentTokens.get() + refillRate);
                        currentTokens.set(newTokens); // Refill tokens
                    } finally {
                        locker.writeLock().unlock(); // Release write lock
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Handle interruption
                }
            }
        }).start();
    }

    /**
     * Gets the retry-after duration in seconds if the rate limit is exceeded.
     *
     * @param userId The user identifier (not used in this implementation).
     * @return Retry-after duration in seconds.
     */
    @Override
    public long getRetryAfter(String userId) {
        // Returns the number of seconds to wait based on current token availability
        locker.readLock().lock(); // Acquire read lock
        try {
            return currentTokens.get() <= 0 ? 1 : 0; // Simplified retry logic
        } finally {
            locker.readLock().unlock(); // Release read lock
        }
    }
}
