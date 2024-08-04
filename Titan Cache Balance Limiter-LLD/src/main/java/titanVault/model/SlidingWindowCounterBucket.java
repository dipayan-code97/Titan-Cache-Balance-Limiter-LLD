package titanVault.model;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Sliding Window Counter Bucket for the Sliding Window Counter Rate Limiter.
 */
public class SlidingWindowCounterBucket {

    private final long maxCapacity; // Maximum number of requests allowed in the window
    private final Duration windowSlideInterval; // Duration of the sliding window
    private final Clock currentTimer; // Clock to get the current time
    private long windowStartTime; // Start time of the current window
    private long requestCounter; // Number of requests in the current window
    private final ReadWriteLock locker = new ReentrantReadWriteLock(); // Lock for thread safety

    public SlidingWindowCounterBucket(long maxCapacity, Duration windowSlideInterval,
                                      Clock currentTimer, long windowStartTime,
                                      long requestCounter) {
        this.maxCapacity = maxCapacity;
        this.windowSlideInterval = windowSlideInterval;
        this.currentTimer = currentTimer;
        this.windowStartTime = windowStartTime;
        this.requestCounter = requestCounter;
    }

    /**
     * Attempt to allow a request by incrementing the count in the current bucket.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean allowRequest() {
        locker.writeLock().lock(); // Acquire write lock for thread-safe access
        try {
            long currentTime = currentTimer.millis();
            long elapsedTime = (currentTime - windowStartTime);
            long elapsedWindows = elapsedTime / windowSlideInterval.toMillis();
            if (elapsedWindows > 0) {
                // Reset the count based on the number of elapsed windows
                requestCounter = Math.max(0, requestCounter - (int) elapsedWindows);
                windowStartTime += elapsedWindows * windowSlideInterval.toMillis();
            }
            if (requestCounter < maxCapacity) {
                requestCounter++;
                return true;
            } else {
                return false;
            }
        } finally {
            locker.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Add a specific count to the current request count.
     * @param count The count to add.
     */
    public void addRequestCount(long count) {
        locker.writeLock().lock(); // Acquire write lock for thread-safe access
        try {
            long currentTime = currentTimer.millis();
            long elapsedTime = (currentTime - windowStartTime);
            long elapsedWindows = elapsedTime / windowSlideInterval.toMillis();
            if (elapsedWindows > 0) {
                // Reset the count based on the number of elapsed windows
                requestCounter = Math.max(0, requestCounter - (int) elapsedWindows);
                windowStartTime += elapsedWindows * windowSlideInterval.toMillis();
            }
            requestCounter = Math.min(maxCapacity, requestCounter + count);
        } finally {
            locker.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Remove a specific count from the current request count.
     * @param count The count to remove.
     * @return true if the count was removed, false otherwise.
     */
    public boolean removeRequestCount(long count) {
        locker.writeLock().lock(); // Acquire write lock for thread-safe access
        try {
            long currentTime = currentTimer.millis();
            long elapsedTime = (currentTime - windowStartTime);
            long elapsedWindows = elapsedTime / windowSlideInterval.toMillis();
            if (elapsedWindows > 0) {
                // Reset the count based on the number of elapsed windows
                requestCounter = Math.max(0, requestCounter - (int) elapsedWindows);
                windowStartTime += elapsedWindows * windowSlideInterval.toMillis();
            }
            if (requestCounter >= count) {
                requestCounter -= count;
                return true;
            } else {
                return false;
            }
        } finally {
            locker.writeLock().unlock(); // Release write lock
        }
    }

    /**
     * Get the current count of requests.
     *
     * @return The current request count.
     */
    public long getRequestCounter() {
        locker.readLock().lock(); // Acquire read lock for thread-safe access
        try {
            long currentTime = currentTimer.millis();
            long elapsedTime = (currentTime - windowStartTime);
            long elapsedWindows = (elapsedTime / windowSlideInterval.toMillis());
            if (elapsedWindows > 0) {
                // Reset the count based on the number of elapsed windows
                requestCounter = Math.max(0, requestCounter - (int) elapsedWindows);
                windowStartTime += (elapsedWindows * windowSlideInterval.toMillis());
            }
            return requestCounter;
        } finally {
            locker.readLock().unlock(); // Release read lock
        }
    }

    /**
     * Set the request count to a specific value.
     * @param count The count to set.
     */
    public void setRequestCounter(long count) {
        locker.writeLock().lock(); // Acquire write lock for thread-safe access
        try {
            long currentTime = currentTimer.millis();
            long elapsedTime = (currentTime - windowStartTime);
            long elapsedWindows = (elapsedTime / windowSlideInterval.toMillis());
            if (elapsedWindows > 0) {
                // Reset the count based on the number of elapsed windows
                requestCounter = Math.max(0, requestCounter - (int) elapsedWindows);
                windowStartTime += elapsedWindows * windowSlideInterval.toMillis();
            }
            requestCounter = Math.min(maxCapacity, count);
        } finally {
            locker.writeLock().unlock(); // Release write lock
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlidingWindowCounterBucket that)) return false;
        return ((maxCapacity == that.maxCapacity)
                && (windowStartTime == that.windowStartTime)
                && (getRequestCounter() == that.getRequestCounter())
                && Objects.equals(windowSlideInterval, that.windowSlideInterval)
                && Objects.equals(currentTimer, that.currentTimer)
                && Objects.equals(locker, that.locker));
    }

    @Override
    public int hashCode() {
        return (Objects.hash(maxCapacity, windowSlideInterval,
                currentTimer, windowStartTime, getRequestCounter(), locker));
    }

    @Override
    public String toString() {
        return ("SlidingWindowCounterBucket{" +
                "MaxCapacity=" + maxCapacity +
                ", WindowSlideInterval=" + windowSlideInterval +
                ", CurrentTimer=" + currentTimer +
                ", WindowStartTime=" + windowStartTime +
                ", RequestCounter=" + requestCounter +
                ", Locker=" + locker +
                '}');
    }
}
