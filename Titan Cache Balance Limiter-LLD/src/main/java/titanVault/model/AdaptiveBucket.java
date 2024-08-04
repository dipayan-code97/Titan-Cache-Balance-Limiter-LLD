package titanVault.model;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * AdaptiveBucket class represents a bucket with adjustable capacity and thread-safe operations.
 */
public class AdaptiveBucket {

    private long maxCountLimit;  // Maximum allowed count of items in the bucket
    private long currentCount;   // Current count of items in the bucket
    private final ReadWriteLock rwLock; // Lock for thread-safe operations

    /*
     * Constructor to initialize the AdaptiveBucket with an initial limit.
     * @param initialCountLimit The initial limit for the bucket.
     */
    public AdaptiveBucket(long initialCountLimit) {
        this.maxCountLimit = initialCountLimit;
        this.currentCount = 0;
        this.rwLock = new ReentrantReadWriteLock();
    }

    /*
     * Attempts to consume a slot in the bucket.
     * @return true if a slot was successfully consumed, false if the limit is reached.
     */
    public boolean consumeRequest() {
        rwLock.writeLock().lock(); // Acquire the write lock to ensure exclusive access
        try {
            if (currentCount < maxCountLimit) {
                currentCount++; // Increment current count
                return true;
            }
            return false;
        } finally {
            rwLock.writeLock().unlock(); // Release the write lock
        }
    }

    /*
     * Adjusts the limit of the bucket to a new value.
     * @param newLimit The new limit to set for the bucket.
     */
    public void adjustCountLimit(long newLimit) {
        rwLock.writeLock().lock(); // Acquire the write lock to ensure exclusive access
        try {
            this.maxCountLimit = newLimit; // Update the maximum count limit
        } finally {
            rwLock.writeLock().unlock(); // Release the write lock
        }
    }

    /*
     * Resets the current count to zero.
     */
    public void reset() {
        rwLock.writeLock().lock(); // Acquire the write lock to ensure exclusive access
        try {
            currentCount = 0; // Reset current count
        } finally {
            rwLock.writeLock().unlock(); // Release the write lock
        }
    }

    /*
     * Adds a specified value to the current count, ensuring it does not exceed the limit.
     * @param value The value to add to the current count.
     */
    public void add(long value) {
        rwLock.writeLock().lock(); // Acquire the write lock to ensure exclusive access
        try {
            currentCount += value; // Increase current count by the specified value
            if (currentCount > maxCountLimit) {
                currentCount = maxCountLimit; // Ensure the count doesn't exceed the limit
            }
        } finally {
            rwLock.writeLock().unlock(); // Release the write lock
        }
    }

    /*
     * Removes a specified value from the current count, ensuring it does not drop below zero.
     * @param value The value to remove from the current count.
     */
    public void remove(long value) {
        rwLock.writeLock().lock(); // Acquire the write lock to ensure exclusive access
        try {
            currentCount -= value; // Decrease current count by the specified value
            if (currentCount < 0) {
                currentCount = 0; // Ensure the count doesn't go below zero
            }
        } finally {
            rwLock.writeLock().unlock(); // Release the write lock
        }
    }

    /*
     * Retrieves the current count.
     * @return The current count.
     */
    public long get() {
        rwLock.readLock().lock(); // Acquire the read lock to allow concurrent reads
        try {
            return currentCount; // Return the current count
        } finally {
            rwLock.readLock().unlock(); // Release the read lock
        }
    }

    /*
     * Updates the current count to a new value, ensuring it does not exceed the limit.
     * @param newValue The new value to set for the current count.
     */
    public void update(long newValue) {
        rwLock.writeLock().lock(); // Acquire the write lock to ensure exclusive access
        try {
            currentCount = newValue; // Set the current count to the new value
            if (currentCount > maxCountLimit) {
                currentCount = maxCountLimit; // Ensure the count doesn't exceed the limit
            }
        } finally {
            rwLock.writeLock().unlock(); // Release the write lock
        }
    }

    /*
     * Compares this AdaptiveBucket with another object for equality.
     * Two AdaptiveBucket instances are considered equal if they have the same maxCountLimit,
     * currentCount, and rwLock.
     * @param o The object to compare with.
     * @return true if this AdaptiveBucket is equal to the other object, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Check if the objects are the same instance
        if (!(obj instanceof AdaptiveBucket that)) return false; // Check if the other object is an instance of AdaptiveBucket
        return (maxCountLimit == that.maxCountLimit &&
                currentCount == that.currentCount &&
                Objects.equals(rwLock, that.rwLock)); // Compare fields for equality
    }

    /*
     * Returns a hash code value for this AdaptiveBucket.
     * The hash code is computed based on the maxCountLimit, currentCount, and rwLock.
     * @return The hash code value for this AdaptiveBucket.
     */
    @Override
    public int hashCode() {
        return (Objects.hash(maxCountLimit, currentCount, rwLock)); // Generate hash code based on fields
    }

    /*
     * Returns a string representation of this AdaptiveBucket.
     * The string includes the maxCountLimit, currentCount, and rwLock.
     * @return A string representation of this AdaptiveBucket.
     */
    @Override
    public String toString() {
        return ("AdaptiveBucket{" +
                "maxCountLimit=" + maxCountLimit +
                ", currentCount=" + currentCount +
                ", rwLock=" + rwLock +
                '}');
    }
}
