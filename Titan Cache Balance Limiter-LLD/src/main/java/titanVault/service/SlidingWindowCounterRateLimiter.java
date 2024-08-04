package titanVault.service;

import titanVault.model.SlidingWindowCounterBucket;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SlidingWindowCounterRateLimiter implements RateLimiter {

    private final long maxCapacity; // Maximum number of requests allowed in the window
    private final Duration windowSlideInterval; // Duration of the sliding window
    private final Clock currentTimer; // Clock to get the current time
    private final ConcurrentMap<String, SlidingWindowCounterBucket> windowCounterBucket; // Map of user-specific request buckets
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Lock for thread safety

    /**
     * Constructor for SlidingWindowCounterRateLimiter.
     *
     * @param maxCapacity    Maximum number of requests allowed in the window.
     * @param windowSlideInterval  Duration of the sliding window.
     * @param currentTimer       Clock to get the current time.
     */
    public SlidingWindowCounterRateLimiter(long maxCapacity,
                                           Duration windowSlideInterval,
                                           Clock currentTimer) {
        this.maxCapacity = maxCapacity;
        this.windowSlideInterval = windowSlideInterval;
        this.currentTimer = currentTimer;
        this.windowCounterBucket = new ConcurrentSkipListMap<>(); // Initialize the map
    }

    /**
     * Determine if a request from a specific user is allowed based on the sliding window counter algorithm.
     *
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    @Override
    public boolean validateRequest(String userId) {
        readWriteLock.writeLock().lock(); // Acquire write lock for thread-safe access
        try {
            SlidingWindowCounterBucket bucket = windowCounterBucket.computeIfAbsent(userId, key ->
                    new SlidingWindowCounterBucket(maxCapacity, windowSlideInterval,
                            currentTimer, currentTimer.millis(),
                            0)
            );
            return bucket.allowRequest();
        } finally {
            readWriteLock.writeLock().unlock(); // Release write lock
        }
    }
}
