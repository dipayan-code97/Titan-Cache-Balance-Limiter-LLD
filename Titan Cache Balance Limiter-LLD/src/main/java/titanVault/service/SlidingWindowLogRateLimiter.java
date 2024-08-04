package titanVault.service;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import titanVault.model.SlidingWindowLogBucket;

/**
 * Sliding Window Log Rate Limiter.
 */
public class SlidingWindowLogRateLimiter implements RateLimiter {

    private final long maxCapacity;  // Maximum number of requests allowed in the window
    private final Duration windowSlideInterval;  // Size of the time window
    private final Clock currentTimer;  // Clock to get the current time
    private final ConcurrentMap<String, SlidingWindowLogBucket> windowLogBucket; // Map of user-specific request logs
    private final ReadWriteLock lock = new ReentrantReadWriteLock(); // Lock for thread safety

    /**
     * Constructor for SlidingWindowLogRateLimiter.
     *
     * @param maxCapacity Maximum number of requests allowed in the window.
     * @param windowSlideInterval  Size of the time window.
     * @param currentTimer Clock to get the current time.
     */
    public SlidingWindowLogRateLimiter(long maxCapacity,
                                       Duration windowSlideInterval,
                                       Clock currentTimer) {
        this.maxCapacity = maxCapacity;
        this.windowSlideInterval = windowSlideInterval;
        this.currentTimer = currentTimer;
        this.windowLogBucket = new ConcurrentSkipListMap<>(); // Initialize the map
    }

    /**
     * Determine if a request from a specific user is allowed based on the sliding window algorithm.
     *
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    @Override
    public boolean validateRequest(String userId) {
        lock.writeLock().lock(); // Acquire write lock to ensure thread-safe access
        try {
            SlidingWindowLogBucket bucket = windowLogBucket.computeIfAbsent(userId, key ->
                    new SlidingWindowLogBucket(maxCapacity, windowSlideInterval, currentTimer)
            );
            return bucket.allowRequest();
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }
}
