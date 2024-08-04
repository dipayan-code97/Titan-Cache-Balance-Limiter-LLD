package titanVault.model;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Leaky Bucket for the Leaky Bucket Rate Limiter.
 */
public class LeakyBucket {

    private final long maxCapacity; // Maximum capacity of the bucket
    private long waterCounter; // Current amount of water in the bucket
    private final double waterLeakRate; // Rate at which water leaks from the bucket
    private final Duration leakageTimePeriod; // Time period for leakage
    private final Clock currentTimer; // Clock to get the current time
    private long lastLeakTime; // Last time the bucket was leaked
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Lock for thread safety

    public LeakyBucket(long maxCapacity, long waterCounter,
                       double waterLeakRate, Duration leakageTimePeriod,
                       Clock currentTimer) {
        this.maxCapacity = maxCapacity;
        this.waterCounter = waterCounter;
        this.waterLeakRate = waterLeakRate;
        this.leakageTimePeriod = leakageTimePeriod;
        this.currentTimer = currentTimer;
        this.lastLeakTime = currentTimer.millis(); // Initialize lastLeakTime to current time
    }

    /**
     * Attempt to allow a request by adding water to the bucket and leaking.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean allowRequest() {
        readWriteLock.writeLock().lock();
        try {
            leak(); // Leak the bucket based on elapsed time
            if (waterCounter < maxCapacity) {
                waterCounter++;
                return true;
            } else {
                return false;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Leak the bucket based on the time elapsed since the last leak.
     */
    private void leak() {
        long currentTime = currentTimer.millis();
        long elapsed = currentTime - lastLeakTime;
        double leaked = (elapsed / (double) leakageTimePeriod.toMillis()) * waterLeakRate;
        waterCounter = Math.max(0, waterCounter - (long) leaked);
        lastLeakTime = currentTime;
    }

    /**
     * Add water to the bucket. If the bucket overflows, it's capped at the maximum capacity.
     * @param amount The amount of water to add.
     */
    public void fill(long amount) {
        readWriteLock.writeLock().lock();
        try {
            leak(); // Ensure leakage is accounted for before adding new water
            waterCounter = Math.min(maxCapacity, waterCounter + amount);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Remove water from the bucket.
     * @param amount The amount of water to remove.
     * @return The actual amount removed.
     */
    public long drop(long amount) {
        readWriteLock.writeLock().lock();
        try {
            leak(); // Ensure leakage is accounted for before removing water
            long removed = Math.min(amount, waterCounter);
            waterCounter -= removed;
            return removed;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Get the current amount of water in the bucket.
     * @return The current amount of water in the bucket.
     */
    public long getMeniscus() {
        readWriteLock.readLock().lock();
        try {
            leak(); // Ensure leakage is accounted for before getting the element
            return waterCounter;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Set the current amount of water in the bucket, adjusting for leakage.
     * @param amount The new amount of water to set in the bucket.
     */
    public void setBucket(long amount) {
        readWriteLock.writeLock().lock();
        try {
            leak(); // Ensure leakage is accounted for before setting the new value
            waterCounter = Math.min(maxCapacity, amount);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Update the current amount of water in the bucket, adjusting for leakage.
     * @param amount The amount of water to add to the current amount.
     */
    public void updateBucket(long amount) {
        readWriteLock.writeLock().lock();
        try {
            leak(); // Ensure leakage is accounted for before updating the value
            waterCounter = Math.min(maxCapacity, waterCounter + amount);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeakyBucket that)) return false;
        return ((maxCapacity == that.maxCapacity)
                && (waterCounter == that.waterCounter)
                && (Double.compare(waterLeakRate, that.waterLeakRate) == 0)
                && (lastLeakTime == that.lastLeakTime)
                && (Objects.equals(leakageTimePeriod, that.leakageTimePeriod))
                && (Objects.equals(currentTimer, that.currentTimer))
                && (Objects.equals(readWriteLock, that.readWriteLock)));
    }

    @Override
    public int hashCode() {
        return (Objects.hash(maxCapacity, waterCounter,
                waterLeakRate, leakageTimePeriod,
                currentTimer, lastLeakTime, readWriteLock));
    }

    @Override
    public String toString() {
        return ("LeakyBucket{" +
                "MaxCapacity=" + maxCapacity +
                ", WaterCounter=" + waterCounter +
                ", WaterLeakRate=" + waterLeakRate +
                ", LeakageTimePeriod=" + leakageTimePeriod +
                ", CurrentTimer=" + currentTimer +
                ", LastLeakTime=" + lastLeakTime +
                ", ReadWriteLock=" + readWriteLock +
                '}');
    }
}
