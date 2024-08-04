package titanVault.service;

import titanVault.model.FixedWindowBucket;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Fixed Window Rate Limiter.
 */
public class FixedWindowServiceRateLimiter implements RateLimiter {

    private final long maxCapacity; // Maximum number of requests in the window
    private final Duration windowInterval; // Duration of the window
    private final Clock currentTimer; // Clock to get the current time
    private final ConcurrentMap<String, FixedWindowBucket> fixedWindowBucket = new ConcurrentSkipListMap<>(); // Stores fixed window buckets for users
    private final ReadWriteLock rwLocker = new ReentrantReadWriteLock(); // Lock for thread safety

    public FixedWindowServiceRateLimiter(long maxCapacity, Duration windowInterval,
                                         Clock currentTimer) {
        this.maxCapacity = maxCapacity;
        this.windowInterval = windowInterval;
        this.currentTimer = currentTimer;
    }

    /**
     * Determine if a request is allowed based on the fixed window algorithm.
     *
     * @param userId The user identifier
     * @return true if the request is allowed, false otherwise
     */
    @Override
    public boolean validateRequest(String userId) {
        rwLocker.writeLock().lock(); // Acquire write lock for thread-safe access
        try {
            FixedWindowBucket bucket = fixedWindowBucket.computeIfAbsent(userId, id ->
                    new FixedWindowBucket(maxCapacity, currentTimer.millis(), 0, windowInterval, currentTimer)
            );
            return bucket.allowRequest();
        } finally {
            rwLocker.writeLock().unlock(); // Release write lock
        }
    }


    /**
     * Updates the rate limiter configuration (max capacity and window interval).
     *
     * @param newMaxCapacity New maximum number of requests allowed in the window.
     * @param newWindowInterval New duration of the window.
     */
    public FixedWindowServiceRateLimiter updateConfiguration(long newMaxCapacity, Duration newWindowInterval) {
        rwLocker.writeLock().lock(); // Acquire write lock for thread-safe update
        try {
            // Return a new instance with the updated configuration
            return new FixedWindowServiceRateLimiter(newMaxCapacity, newWindowInterval, currentTimer);
        } finally {
            rwLocker.writeLock().unlock(); // Release write lock
        }
    }
}
