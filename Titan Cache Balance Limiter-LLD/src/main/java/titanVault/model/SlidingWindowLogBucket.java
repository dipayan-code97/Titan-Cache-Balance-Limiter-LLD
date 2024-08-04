package titanVault.model;

import java.time.Clock;
import java.time.Duration;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Sliding Window Log Bucket for the Sliding Window Log Rate Limiter.
 */

public class SlidingWindowLogBucket {

    private final long maxCapacity; // Maximum number of requests allowed in the window
    private final Duration windowSlideInterval; // Duration of the sliding window
    private final Clock currentTimeClock; // Clock to get the current time
    private final Deque<Long> logRequests = new ConcurrentLinkedDeque<>(); // Log | stack of requested timestamps
    private final ReadWriteLock lock = new ReentrantReadWriteLock(); // Read-Write lock for thread safety

    public SlidingWindowLogBucket(long maxCapacity,
                                  Duration windowSlideInterval,
                                  Clock currentTimeClock) {
        this.maxCapacity = maxCapacity;
        this.windowSlideInterval = windowSlideInterval;
        this.currentTimeClock = currentTimeClock;
    }

    /**
     * Clean timestamps outside the sliding window.
     * @param currentTime The current time in milliseconds.
     */
    private void cleanOldTimestamps(long currentTime) {
        while (!logRequests.isEmpty() && (currentTime - logRequests.peekLast()) > windowSlideInterval.toMillis()) {
            logRequests.pollLast();
        }
    }

    /**
     * Attempt to allow a request by adding the current timestamp to the log.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean allowRequest() {
        long currentTime = currentTimeClock.millis();
        lock.writeLock().lock(); // Use write lock to ensure exclusive access while cleaning and modifying the log
        try {
            cleanOldTimestamps(currentTime); // Clean timestamps outside the sliding window
            if (logRequests.size() < maxCapacity) {
                logRequests.push(currentTime); // Add the current request timestamp to the top of the stack
                return true; // Request is allowed
            } else {
                return false; // Request is denied
            }
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Add a specific timestamp to the log.
     * @param timestamp The timestamp to add.
     */
    public void addTimestamp(long timestamp) {
        lock.writeLock().lock(); // Acquire write lock
        try {
            cleanOldTimestamps(timestamp); // Clean timestamps outside the sliding window
            if (logRequests.size() < maxCapacity) {
                logRequests.push(timestamp); // Add the timestamp to the top of the stack
            }
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Remove a specific timestamp from the log.
     * @param timestamp The timestamp to remove.
     * @return true if the timestamp was removed, false otherwise.
     */
    public boolean removeTimestamp(long timestamp) {
        lock.writeLock().lock(); // Acquire write lock
        try {
            return logRequests.remove(timestamp); // Remove timestamp from the stack
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Get the current number of timestamps in the log.
     * @return The number of timestamps.
     */
    public long getTimestamp() {
        lock.readLock().lock(); // Acquire read lock
        try {
            return logRequests.size(); // Return the size of the stack
        } finally {
            lock.readLock().unlock(); // Release read lock
        }
    }

    /**
     * Set the log to a specific set of timestamps.
     * @param timestamps A collection of timestamps to set.
     */
    public void setTimestamp(Deque<Long> timestamps) {
        lock.writeLock().lock(); // Acquire write lock
        try {
            logRequests.clear(); // Clear the existing timestamps
            cleanOldTimestamps(currentTimeClock.millis()); // Ensure we are within the window
            logRequests.addAll(timestamps); // Add all new timestamps to the stack
        } finally {
            lock.writeLock().unlock(); // Release write lock
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlidingWindowLogBucket that)) return false;
        return (maxCapacity == that.maxCapacity
                && Objects.equals(windowSlideInterval, that.windowSlideInterval)
                && Objects.equals(currentTimeClock, that.currentTimeClock)
                && Objects.equals(logRequests, that.logRequests)
                && Objects.equals(lock, that.lock));
    }

    @Override
    public int hashCode() {
        return (Objects.hash(maxCapacity, windowSlideInterval,
                currentTimeClock, logRequests, lock));
    }

    @Override
    public String toString() {
        return ("SlidingWindowLogBucket{" +
                "MaxCapacity=" + maxCapacity +
                ", WindowSlideInterval=" + windowSlideInterval +
                ", CurrentTimeClock=" + currentTimeClock +
                ", LogRequests=" + logRequests +
                ", Lock=" + lock +
                '}');
    }
}
