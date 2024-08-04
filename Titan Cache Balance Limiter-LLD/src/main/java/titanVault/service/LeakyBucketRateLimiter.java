package titanVault.service;

import titanVault.model.LeakyBucket;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Leaky bucket rate limiter implementation of the RateLimiter interface.
 */
public abstract class LeakyBucketRateLimiter implements RateLimiter {

    private final long maxCapacity; // Maximum capacity of the bucket
    private final double leakRate; // Rate at which water leaks from the bucket
    private final Duration leakPeriod; // Time period for leakage
    private final Clock currentTimer; // Clock to get the current time
    private final ConcurrentMap<String, LeakyBucket> leakyBucket = new ConcurrentSkipListMap<>(); // Stores leaky buckets for users
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Lock for thread safety

    public LeakyBucketRateLimiter(long maxCapacity, double leakRate,
                                  Duration leakPeriod, Clock currentTimer) {
        this.maxCapacity = maxCapacity;
        this.leakRate = leakRate;
        this.leakPeriod = leakPeriod;
        this.currentTimer = currentTimer;
    }

    /**
     * Determine if a request is allowed based on the leaky bucket algorithm.
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    @Override
    public boolean validateRequest(String userId) {
        LeakyBucket bucket;
        readWriteLock.readLock().lock();
        try {
            bucket = leakyBucket.get(userId);
        } finally {
            readWriteLock.readLock().unlock();
        }

        if (bucket == null) {
            readWriteLock.writeLock().lock();
            try {
                bucket = leakyBucket.computeIfAbsent(userId, key ->
                        new LeakyBucket(maxCapacity, 0, leakRate, leakPeriod, currentTimer));
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }

        return bucket.allowRequest();
    }

    /**
     * Get the leaky bucket associated with a user.
     * @param userId The user identifier.
     * @return The LeakyBucket instance for the given user.
     */
    public LeakyBucket getLeakyBucket(String userId) {
        readWriteLock.readLock().lock();
        try {
            return leakyBucket.get(userId);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
