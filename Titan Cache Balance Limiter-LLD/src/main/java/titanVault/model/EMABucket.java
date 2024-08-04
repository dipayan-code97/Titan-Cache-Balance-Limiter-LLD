package titanVault.model;

import titanVault.config.EMAConfig;

import java.time.Clock;
import java.time.Duration;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * EMABucket implements an Exponential Moving Average (EMA) algorithm for rate limiting.
 * It uses a ReadWriteLock to ensure thread safety.
 */
public class EMABucket {

    private double exponentialMovingAverage;                // Current EMA value
    private long lastRequestTime;                           // Time of the last request
    private final Clock currentTimer;                              // Clock to get the current time
    private final ReadWriteLock threadLocker = new ReentrantReadWriteLock(); // Lock for thread safety
    private final Deque<Long> requestTimestamps = new ConcurrentLinkedDeque<>();
    private final AtomicReference<EMAConfig> configReference;  // AtomicReference for mutable configuration

    /**
     * Constructor to initialize EMABucket.
     *
     * @param maxCapacity     Maximum number of requests allowed.
     * @param averagingPeriod Duration over which EMA is calculated.
     * @param currentTimer    Clock to get the current time.
     */
    public EMABucket(long maxCapacity, Duration averagingPeriod, Clock currentTimer) {
        this.configReference = new AtomicReference<>(new EMAConfig(maxCapacity, averagingPeriod));
        this.exponentialMovingAverage = 0.0D;
        this.lastRequestTime = currentTimer.millis();
        this.currentTimer = currentTimer;
    }

    /**
     * Gets the current value of the exponential moving average.
     * Uses a read lock to ensure thread safety during read operations.
     *
     * @return the current exponential moving average value.
     */
    public double getExponentialMovingAverage() {
        threadLocker.readLock().lock();  // Acquire read lock to read the state
        try {
            return this.exponentialMovingAverage;
        } finally {
            threadLocker.readLock().unlock();  // Release read lock
        }
    }

    /**
     * Determines if a request is allowed based on the EMA rate limiting algorithm.
     * Uses a write lock to ensure thread safety during the update.
     *
     * @return true if the request is allowed, false otherwise.
     */
    public boolean allowRequest() {
        threadLocker.writeLock().lock();  // Acquire write lock to update the state
        try {
            long currentTime = currentTimer.millis();
            long elapsedTime = currentTime - lastRequestTime;

            // Retrieve the current alpha and maxCapacity from the configuration
            EMAConfig config = configReference.get();
            double alpha = config.getAlpha();
            double maxCapacity = config.getMaxCapacity();

            // Calculate the new EMA based on the current configuration
            exponentialMovingAverage += alpha * (elapsedTime - exponentialMovingAverage);

            // Update the last request time
            lastRequestTime = currentTime;

            // Check if the request is within allowed capacity
            return exponentialMovingAverage <= maxCapacity;
        } finally {
            threadLocker.writeLock().unlock();  // Release write lock
        }
    }

    /**
     * Updates the bucket configuration without changing final fields.
     * The new configuration will be used for subsequent operations.
     *
     * @param newMaxCapacity New maximum number of requests allowed.
     * @param newAveragingPeriod New duration over which EMA is calculated.
     */
    public void updateBucket(long newMaxCapacity, Duration newAveragingPeriod) {
        threadLocker.writeLock().lock();  // Acquire write lock to safely update the configuration
        try {
            // Update the mutable configuration
            configReference.set(new EMAConfig(newMaxCapacity, newAveragingPeriod));

            // Recalculate EMA to reflect the new averaging period
            recalculateEMA();
        } finally {
            threadLocker.writeLock().unlock();  // Release write lock
        }
    }

    /**
     * Removes the oldest request from the bucket, adjusting the EMA accordingly.
     * Uses a write lock to ensure thread safety during the update.
     *
     * @return true if the request was successfully removed, false otherwise.
     */
    public boolean removeRequest() {
        threadLocker.writeLock().lock();  // Acquire write lock to update the state
        try {
            if (requestTimestamps.isEmpty()) {
                return false;  // No requests to remove
            }

            // Remove the oldest request timestamp
            long removedRequestTime = requestTimestamps.removeFirst();

            // Recalculate EMA without the removed request
            recalculateEMA();

            // Adjust lastRequestTime if necessary
            if (!requestTimestamps.isEmpty()) {
                lastRequestTime = requestTimestamps.getLast();
            }

            return true;
        } finally {
            threadLocker.writeLock().unlock();  // Release write lock
        }
    }

    /**
     * Recalculates the EMA based on the remaining request timestamps.
     */
    private void recalculateEMA() {
        if (requestTimestamps.isEmpty()) {
            exponentialMovingAverage = 0;
            return;
        }

        long currentTime = currentTimer.millis();
        double ema = 0;
        long prevTimestamp = currentTime;

        // Retrieve the current alpha from the configuration
        EMAConfig config = configReference.get();
        double alpha = config.getAlpha();

        for (long timestamp : requestTimestamps) {
            long elapsedTime = prevTimestamp - timestamp;
            ema += alpha * (elapsedTime - ema);
            prevTimestamp = timestamp;
        }

        exponentialMovingAverage = ema;
    }

    /**
     * Determines if this EMABucket is equal to another object.
     * Two EMABucket instances are considered equal if they have the same maxCapacity, alpha, and lastRequestTime.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EMABucket emaBucket)) return false;
        return ((configReference.get().getMaxCapacity() == emaBucket.configReference.get().getMaxCapacity())
                && (Double.compare(configReference.get().getAlpha(), emaBucket.configReference.get().getAlpha()) == 0)
                && (Double.compare(getExponentialMovingAverage(), emaBucket.getExponentialMovingAverage()) == 0)
                && (lastRequestTime == emaBucket.lastRequestTime)
                && (Objects.equals(currentTimer, emaBucket.currentTimer))
                && (Objects.equals(threadLocker, emaBucket.threadLocker)));
    }

    /**
     * Computes the hash code for this EMABucket.
     * The hash code is based on maxCapacity, alpha, and lastRequestTime.
     *
     * @return the hash code of this EMABucket
     */
    @Override
    public int hashCode() {
        return Objects.hash(configReference.get().getMaxCapacity(), configReference.get().getAlpha(), lastRequestTime);
    }

    /**
     * Returns a string representation of the EMABucket instance.
     * This method provides a detailed description of the state of the EMABucket object.
     * It includes:
     * - `maxCapacity`: The maximum number of requests allowed in the bucket.
     * - `alpha`: The smoothing factor used for the Exponential Moving Average (EMA) calculation.
     * - `exponentialMovingAverage`: The current value of the Exponential Moving Average.
     * - `lastRequestTime`: The timestamp of the last request made.
     * - `clock`: The Clock instance used to get the current time.
     *
     * @return A string representation of the EMABucket instance.
     */
    @Override
    public String toString() {
        return "EMABucket{" +
                "maxCapacity=" + configReference.get().getMaxCapacity() +
                ", alpha=" + configReference.get().getAlpha() +
                ", exponentialMovingAverage=" + exponentialMovingAverage +
                ", lastRequestTime=" + lastRequestTime +
                ", clock=" + currentTimer +
                ", threadLocker=" + threadLocker +
                '}';
    }
}
