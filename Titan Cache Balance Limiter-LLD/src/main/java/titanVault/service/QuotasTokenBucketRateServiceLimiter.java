package titanVault.service;

import titanVault.model.Quota;
import titanVault.model.TokenBucket;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Rate limiter that combines token bucket rate limiting with quota management.
 */
public class QuotasTokenBucketRateServiceLimiter implements RateLimiter {

    private final long maxCapacity; // Maximum number of tokens in the bucket
    private final Duration refillPeriod; // Time period for token refill
    private final long tokensPerPeriod; // Number of tokens added per period
    private final Clock currentTimer; // Clock to get the current time
    private final ConcurrentMap<String, TokenBucket> tokenBuckets; // Token buckets for users
    private final ConcurrentMap<String, Quota> quotas; // Quotas for users
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Lock for managing concurrency

    /**
     * Constructor to initialize the QuotasTokenBucketRateServiceLimiter.
     *
     * @param maxCapacity     Maximum number of tokens in the bucket.
     * @param refillPeriod    Duration of the refill period.
     * @param tokensPerPeriod Number of tokens added per period.
     * @param currentTimer    Clock to get the current time.
     * @param maxQuota        Maximum quota allowed.
     * @param quotaPeriod     Duration of the quota period.
     */
    public QuotasTokenBucketRateServiceLimiter(long maxCapacity, Duration refillPeriod,
                                               long tokensPerPeriod, Clock currentTimer,
                                               long maxQuota, Duration quotaPeriod) {
        this.maxCapacity = maxCapacity;
        this.refillPeriod = refillPeriod;
        this.tokensPerPeriod = tokensPerPeriod;
        this.currentTimer = currentTimer;
        this.tokenBuckets = new ConcurrentSkipListMap<>();
        this.quotas = new ConcurrentSkipListMap<>();

        // Initialize quotas for users (if needed, can be customized)
        // Example initialization: this.quotas.put("defaultUser", new Quota(maxQuota, quotaPeriod, currentTimer));
    }

    /**
     * Validate a request based on both token bucket and quota.
     *
     * @param userId       The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    @Override
    public boolean validateRequest(String userId) {
        long minRequestAmount = 1;
        return validateRequest(userId, minRequestAmount); // Default requestAmount to 1 if not provided
    }

    /**
     * Validate a request based on both token bucket and quota with specified amount.
     *
     * @param userId       The user identifier.
     * @param requestAmount The amount of resource requested.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean validateRequest(String userId, long requestAmount) {
        readWriteLock.readLock().lock();
        TokenBucket bucket = null;
        Quota quota = null;
        try {
            // Compute token bucket for the user
            bucket = tokenBuckets.computeIfAbsent(userId, key -> new TokenBucket(
                    maxCapacity, tokensPerPeriod, refillPeriod, currentTimer
            ));
            // Compute quota for the user
            quota = quotas.computeIfAbsent(userId, key -> new Quota(
                    1000, // example quota, customize as needed
                    Duration.ofHours(1),
                    currentTimer
            ));
        } finally {
            readWriteLock.readLock().unlock();
        }

        readWriteLock.writeLock().lock();
        try {
            // Check if request can be allowed based on quota and token bucket
            if ((quota != null) && (bucket != null) && (quota.consume(requestAmount) && bucket.consume())) {
                return true;
            } else {
                return false;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Get the remaining quota for a specific user.
     *
     * @param userId The user identifier.
     * @return The remaining quota.
     */
    public long getRemainingQuota(String userId) {
        readWriteLock.readLock().lock();
        try {
            Quota quota = quotas.get(userId);
            return quota != null ? quota.getRemainingQuota() : 0;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
