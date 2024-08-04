package titanVault.model;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Quota for rate limiting with usage tracking.
 */
public class Quota {

    private final long maxQuota; // Maximum quota allowed
    private final Duration quotaPeriod; // Period over which the quota is applied
    private final Clock currentTimer; // Clock to get the current time
    private long quotaStartTime; // Start time of the current quota period
    private long usedQuota; // Amount of quota used
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Lock for thread safety

    public Quota(long maxQuota, Duration quotaPeriod, Clock currentTimer) {
        this.maxQuota = maxQuota;
        this.quotaPeriod = quotaPeriod;
        this.currentTimer = currentTimer;
        this.quotaStartTime = currentTimer.millis();
        this.usedQuota = 0;
    }

    public boolean consume(long amount) {
        readWriteLock.writeLock().lock();
        try {
            long currentTime = currentTimer.millis();
            long elapsedTime = currentTime - quotaStartTime;
            if (elapsedTime >= quotaPeriod.toMillis()) {
                // Reset the quota period
                quotaStartTime = currentTime;
                usedQuota = 0;
            }
            if (usedQuota + amount <= maxQuota) {
                usedQuota += amount;
                return true;
            } else {
                return false;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public long getRemainingQuota() {
        readWriteLock.readLock().lock();
        try {
            long currentTime = currentTimer.millis();
            long elapsedTime = currentTime - quotaStartTime;
            if (elapsedTime >= quotaPeriod.toMillis()) {
                // Reset the quota period
                return maxQuota;
            }
            return maxQuota - usedQuota;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Quota quota)) return false;
        return ((maxQuota == quota.maxQuota)
                && (quotaStartTime == quota.quotaStartTime)
                && (usedQuota == quota.usedQuota)
                && (Objects.equals(quotaPeriod, quota.quotaPeriod))
                && (Objects.equals(currentTimer, quota.currentTimer))
                && (Objects.equals(readWriteLock, quota.readWriteLock)));
    }

    @Override
    public int hashCode() {
        return (Objects.hash(maxQuota, quotaPeriod,
                currentTimer, quotaStartTime,
                usedQuota, readWriteLock));
    }
}
