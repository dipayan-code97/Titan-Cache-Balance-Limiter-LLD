package titanVault.model;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a Fixed Window Bucket for the Fixed Window Rate Limiter.
 */
public class FixedWindowBucket {

    private final long maxCapacity; // Maximum number of requests allowed in the window
    private long windowStartTime; // Start time of the current window
    private long requestCounter; // Number of requests in the current window
    private final Duration windowInterval; // Duration of the window
    private final Clock currentTimer; // Clock to get the current time
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Lock for thread safety

    public FixedWindowBucket(long maxCapacity, long windowStartTime,
                             long requestCounter, Duration windowInterval,
                             Clock currentTimer) {
        this.maxCapacity = maxCapacity;
        this.windowStartTime = windowStartTime;
        this.requestCounter = requestCounter;
        this.windowInterval = windowInterval;
        this.currentTimer = currentTimer;
    }

    /**
     * Attempt to allow a request by incrementing the request count.
     * @return true if the request is allowed, false otherwise.
     */
    public boolean allowRequest() {
        readWriteLock.writeLock().lock();
        try {
            long currentTime = currentTimer.millis();
            if ((currentTime - windowStartTime) > windowInterval.toMillis()) {
                // Reset the window if the current time is outside the window duration
                windowStartTime = currentTime;
                requestCounter = 0;
            }
            if (requestCounter < maxCapacity) {
                requestCounter++;
                return true;
            } else {
                return false;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Add a specific number of requests to the counter.
     * @param requests The number of requests to add.
     */
    public void addRequest(long requests) {
        readWriteLock.writeLock().lock();
        try {
            long currentTime = currentTimer.millis();
            if ((currentTime - windowStartTime) > windowInterval.toMillis()) {
                // Reset the window if the current time is outside the window duration
                windowStartTime = currentTime;
                requestCounter = 0;
            }
            requestCounter = Math.min(maxCapacity, requestCounter + requests); // Add requests without exceeding max capacity
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Remove a specific number of requests from the counter.
     * @param requests The number of requests to remove.
     * @return The actual number of requests removed.
     */
    public long removeRequest(long requests) {
        readWriteLock.writeLock().lock();
        try {
            long currentTime = currentTimer.millis();
            if ((currentTime - windowStartTime) > windowInterval.toMillis()) {
                // Reset the window if the current time is outside the window duration
                windowStartTime = currentTime;
                requestCounter = 0;
            }
            long removed = Math.min(requests, requestCounter); // Remove requests without going below zero
            requestCounter -= removed;
            return removed;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Get the current number of requests in the bucket.
     * @return The current number of requests in the bucket.
     */
    public long getRequest() {
        readWriteLock.readLock().lock();
        try {
            long currentTime = currentTimer.millis();
            if ((currentTime - windowStartTime) > windowInterval.toMillis()) {
                // Reset the window if the current time is outside the window duration
                readWriteLock.writeLock().lock();
                try {
                    windowStartTime = currentTime;
                    requestCounter = 0;
                } finally {
                    readWriteLock.writeLock().unlock();
                }
            }
            return requestCounter;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Set the number of requests in the bucket to a specific value.
     * @param request The number of requests to set.
     */
    public void setRequest(long request) {
        readWriteLock.writeLock().lock();
        try {
            long currentTime = currentTimer.millis();
            if ((currentTime - windowStartTime) > windowInterval.toMillis()) {
                // Reset the window if the current time is outside the window duration
                windowStartTime = currentTime;
                requestCounter = 0;
            }
            requestCounter = Math.min(maxCapacity, request); // Set requests without exceeding max capacity
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
