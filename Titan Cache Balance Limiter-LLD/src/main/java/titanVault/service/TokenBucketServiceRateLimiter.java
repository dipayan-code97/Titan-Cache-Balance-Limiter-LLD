package titanVault.service;

import titanVault.model.TokenBucket;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TokenBucketServiceRateLimiter implements RateLimiter {

    private final long maxCapacity; // Maximum number of tokens in the bucket
    private final Duration refillPeriod; // Time period for token refill
    private final long tokensPerPeriod; // Number of tokens added per period
    private final Clock currentTimer; // Clock to get the current time
    private final ConcurrentMap<String, TokenBucket> tokenBuckets = new ConcurrentSkipListMap<>(); // Stores token buckets for users
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Lock for thread safety

    /**
     * Constructor to initialize the TokenBucketRateLimiter.
     *
     * @param maxCapacity     Maximum capacity of the bucket
     * @param refillPeriod    Duration of the refill period
     * @param tokensPerPeriod Number of tokens added per period
     * @param currentTimer    Clock to get the current time
     */
    public TokenBucketServiceRateLimiter(long maxCapacity, Duration refillPeriod,
                                         long tokensPerPeriod, Clock currentTimer) {
        this.maxCapacity = maxCapacity;
        this.refillPeriod = refillPeriod;
        this.tokensPerPeriod = tokensPerPeriod;
        this.currentTimer = currentTimer;
    }

    /**
     * Determine if a request is allowed based on the token bucket algorithm.
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    @Override
    public boolean validateRequest(String userId) {
        readWriteLock.readLock().lock();
        try {
            TokenBucket bucket = tokenBuckets.computeIfAbsent(userId, key -> {
                readWriteLock.readLock().unlock(); // Unlock read lock before upgrading to write lock
                readWriteLock.writeLock().lock();
                try {
                    // Double-check if the bucket is still absent
                    return tokenBuckets.computeIfAbsent(key, k -> new TokenBucket(
                            maxCapacity, tokensPerPeriod, refillPeriod, currentTimer
                    ));
                } finally {
                    readWriteLock.writeLock().unlock();
                }
            });

            readWriteLock.readLock().lock(); // Re-acquire read lock
            bucket.refill();
            return bucket.consume();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
