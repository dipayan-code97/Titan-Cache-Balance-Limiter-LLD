package titanVault.model;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Token Bucket for the Token Bucket Rate Limiter.
 */
public class TokenBucket {

    private final long maxCapacity; // Maximum capacity of the bucket
    private final long tokensPerPeriod; // Number of tokens added per period
    private final Duration refillPeriod; // Duration of the refill period
    private long lastRefillTime; // Timestamp of the last refill
    private long tokenCounter; // Current number of tokens in the bucket
    private final Clock currentTimer; // Clock to get the current time

    private final ReadWriteLock lock = new ReentrantReadWriteLock(); // ReadWriteLock for concurrency control

    /**
     * Constructor to initialize the TokenBucket with specified parameters.
     * @param maxCapacity Maximum capacity of the bucket
     * @param tokensPerPeriod Number of tokens added per period
     * @param refillPeriod Duration of the refill period
     * @param currentTimer Clock to get the current time
     */
    public TokenBucket(long maxCapacity, long tokensPerPeriod,
                       Duration refillPeriod, Clock currentTimer) {
        this.maxCapacity = maxCapacity;
        this.tokensPerPeriod = tokensPerPeriod;
        this.refillPeriod = refillPeriod;
        this.currentTimer = currentTimer;
        this.lastRefillTime = currentTimer.millis();
        this.tokenCounter = maxCapacity; // Start with full capacity
    }

    /**
     * Refill the bucket with tokens based on elapsed time.
     */
    public void refill() {
        lock.writeLock().lock(); // Acquire write lock for refilling
        try {
            long currentTime = currentTimer.millis(); // Get the current time in milliseconds
            long recentRefillElapsedTime = currentTime - lastRefillTime; // Calculate elapsed time since last refill

            if (recentRefillElapsedTime > 0) {
                processTokenRefill(recentRefillElapsedTime); // Process the token refill based on elapsed time
            }
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Process the token refill logic based on elapsed time.
     * @param elapsedMillis The elapsed time in milliseconds since the last refill.
     */
    private void processTokenRefill(long elapsedMillis) {
        long refillPeriodsElapsed = elapsedMillis / refillPeriod.toMillis(); // Calculate the number of complete refill periods that have elapsed
        if (refillPeriodsElapsed > 0) {
            long tokensToAdd = refillPeriodsElapsed * tokensPerPeriod; // Calculate the number of tokens to add
            tokenCounter = Math.min(maxCapacity, tokenCounter + tokensToAdd); // Add tokens to the bucket without exceeding max capacity

            // Update the last refill time
            lastRefillTime += refillPeriodsElapsed * refillPeriod.toMillis();
        }
    }

    /**
     * Attempt to consume a token from the bucket.
     * @return true if a token was successfully consumed, false otherwise.
     */
    public boolean consume() {
        lock.writeLock().lock(); // Acquire write lock for consuming
        try {
            if (tokenCounter > 0) {
                --tokenCounter;
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Add a specific number of tokens to the bucket.
     * @param token The number of tokens to add.
     */
    public void addToken(long token) {
        lock.writeLock().lock(); // Acquire write lock for adding tokens
        try {
            refill(); // Ensure the bucket is refilled before adding tokens
            tokenCounter = Math.min(maxCapacity, tokenCounter + token); // Add tokens without exceeding max capacity
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Remove a specific number of tokens from the bucket.
     * @param token The number of tokens to remove.
     * @return The actual number of tokens removed.
     */
    public long removeToken(long token) {
        lock.writeLock().lock(); // Acquire write lock for removing tokens
        try {
            refill(); // Ensure the bucket is refilled before removing tokens
            long removed = Math.min(token, tokenCounter); // Remove tokens without exceeding the current count
            tokenCounter -= removed;
            return removed;
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Get the current number of tokens in the bucket.
     * @return The current number of tokens in the bucket.
     */
    public long getToken() {
        lock.readLock().lock(); // Acquire read lock for getting tokens
        try {
            refill(); // Ensure the bucket is refilled before getting the current count
            return tokenCounter;
        } finally {
            lock.readLock().unlock(); // Release read lock
        }
    }

    /**
     * Set the number of tokens in the bucket to a specific value.
     * @param token The number of tokens to set.
     */
    public void setToken(long token) {
        lock.writeLock().lock(); // Acquire write lock for setting tokens
        try {
            refill(); // Ensure the bucket is refilled before setting the new value
            tokenCounter = Math.min(maxCapacity, token); // Set tokens without exceeding max capacity
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenBucket that)) return false;
        return ((maxCapacity == that.maxCapacity)
                && (tokensPerPeriod == that.tokensPerPeriod)
                && (lastRefillTime == that.lastRefillTime)
                && (tokenCounter == that.tokenCounter)
                && (Objects.equals(refillPeriod, that.refillPeriod))
                && (Objects.equals(currentTimer, that.currentTimer))
                && (Objects.equals(lock, that.lock)));
    }

    @Override
    public int hashCode() {
        return (Objects.hash(maxCapacity, tokensPerPeriod,
                refillPeriod, lastRefillTime,
                tokenCounter, currentTimer, lock));
    }
}
