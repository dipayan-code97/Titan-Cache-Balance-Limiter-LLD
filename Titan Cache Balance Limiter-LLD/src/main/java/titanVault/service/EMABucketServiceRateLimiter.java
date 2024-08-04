package titanVault.service;

import titanVault.model.EMABucket;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Rate limiter implementation using the Exponential Moving Average (EMA) algorithm.
*/
public class EMABucketServiceRateLimiter implements RateLimiter {

    private final long maxCapacity;               // Maximum number of requests allowed
    private final Duration averagingPeriod;      // Duration over which EMA is calculated
    private final Clock currentTimer;            // Clock to get the current time
    private final ConcurrentMap<String, EMABucket> emaBucketStore = new ConcurrentSkipListMap<>(); // Stores EMA buckets for users
    private final ReadWriteLock configLog = new ReentrantReadWriteLock(); // Lock for configuration updates

    /**
     * Constructor to initialize the rate limiter with parameters.
     *
     * @param maxCapacity     Maximum number of requests allowed.
     * @param averagingPeriod Duration over which EMA is calculated.
     * @param currentTimer    Clock instance to get the current time.
     */
    public EMABucketServiceRateLimiter(long maxCapacity, Duration averagingPeriod, Clock currentTimer) {
        this.maxCapacity = maxCapacity;
        this.averagingPeriod = averagingPeriod;
        this.currentTimer = currentTimer;
    }

    /**
     * Determine if a request is allowed based on the EMA algorithm.
     *
     * @param userId The user identifier
     * @return true if the request is allowed, false otherwise
     */
    @Override
    public boolean validateRequest(String userId) {
        EMABucket bucket = emaBucketStore.computeIfAbsent(userId, id ->
                new EMABucket(maxCapacity, averagingPeriod, currentTimer)
        );
        return bucket.allowRequest();
    }

    /**
     * Updates the configuration for all user buckets.
     * Acquires write lock to ensure thread-safe updates to the configuration.
     *
     * @param newMaxCapacity     New maximum number of requests allowed.
     * @param newAveragingPeriod New duration over which EMA is calculated.
     */
    public void updateConfiguration(long newMaxCapacity, Duration newAveragingPeriod) {
        configLog.writeLock().lock(); // Acquire write lock to update configuration
        try {
            for (EMABucket bucket : emaBucketStore.values()) {
                bucket.updateBucket(newMaxCapacity, newAveragingPeriod);
            }
        } finally {
            configLog.writeLock().unlock(); // Release write lock
        }
    }
}