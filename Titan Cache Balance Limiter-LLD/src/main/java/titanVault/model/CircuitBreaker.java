package titanVault.model;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CircuitBreaker {
    // Enum to represent the state of the circuit breaker
    private enum State { CLOSED, OPEN, HALF_OPEN }

    private final long failureThreshold; // Number of failures before opening the circuit
    private final long resetTimeoutMillis; // Time in milliseconds to wait before moving from OPEN to HALF_OPEN
    private State state = State.CLOSED; // Initial state of the circuit breaker
    private long lastFailureTime; // Timestamp of the last failure
    private long failureCount; // Count of consecutive failures
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // Lock for thread safety

    /**
     * Constructor to initialize the CircuitBreaker.
     *
     * @param failureThreshold The number of failures that trigger the circuit breaker to open.
     * @param resetTimeoutMillis Time in milliseconds to wait before transitioning from OPEN to HALF_OPEN.
     */
    public CircuitBreaker(long failureThreshold, long resetTimeoutMillis) {
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMillis = resetTimeoutMillis;
    }

    /**
     * Checks if a call is allowed based on the current state of the circuit breaker.
     *
     * @return true if the call is allowed, false otherwise.
     */
    public boolean isCallAllowed() {
        readWriteLock.readLock().lock();
        try {
            switch (state) {
                case OPEN:
                    /* If the circuit is OPEN, check if the reset timeout has passed.
                       If it has, transition to HALF_OPEN state. */
                    if (Instant.now().toEpochMilli() - lastFailureTime > resetTimeoutMillis) {
                        readWriteLock.readLock().unlock(); // Unlock read lock before upgrading to write lock
                        readWriteLock.writeLock().lock();
                        try {
                            if (Instant.now().toEpochMilli() - lastFailureTime > resetTimeoutMillis) {
                                state = State.HALF_OPEN;
                            }
                        } finally {
                            readWriteLock.writeLock().unlock();
                        }
                    } else {
                        return false; // Circuit is OPEN and reset timeout hasn't passed yet
                    }
                    break;
                case HALF_OPEN:
                    /* If the circuit is HALF_OPEN, it means we are in a transitional state.
                       Allow the call and transition to CLOSED state if successful. */
                    readWriteLock.readLock().unlock(); // Unlock read lock before upgrading to write lock
                    readWriteLock.writeLock().lock();
                    try {
                        state = State.CLOSED;
                        failureCount = 0; // Reset failure count as we are transitioning to CLOSED
                        return true;
                    } finally {
                        readWriteLock.writeLock().unlock();
                    }
                default:
                    /* If the circuit is CLOSED, the call is allowed. */
                    break;
            }
            return true;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Records a failure, which may trigger the circuit breaker to open.
     */
    public void recordFailure() {
        readWriteLock.writeLock().lock();
        try {
            failureCount++;
            /* If the number of failures exceeds the threshold, open the circuit
               and record the time of the last failure. */
            if (failureCount >= failureThreshold) {
                state = State.OPEN;
                lastFailureTime = Instant.now().toEpochMilli();
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Records a success, which resets the circuit breaker to CLOSED state.
     */
    public void recordSuccess() {
        readWriteLock.writeLock().lock();
        try {
            state = State.CLOSED;
            failureCount = 0; // Reset failure count on success
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CircuitBreaker that)) return false;
        return (failureThreshold == that.failureThreshold
                && resetTimeoutMillis == that.resetTimeoutMillis
                && lastFailureTime == that.lastFailureTime
                && failureCount == that.failureCount
                && state == that.state
                && Objects.equals(readWriteLock, that.readWriteLock));
    }

    @Override
    public int hashCode() {
        return (Objects.hash(failureThreshold, resetTimeoutMillis,
                                state, lastFailureTime,
                                failureCount, readWriteLock));
    }

    @Override
    public String toString() {
        return "CircuitBreaker{" +
                "failureThreshold=" + failureThreshold +
                ", resetTimeoutMillis=" + resetTimeoutMillis +
                ", state=" + state +
                ", lastFailureTime=" + lastFailureTime +
                ", failureCount=" + failureCount +
                ", readWriteLock=" + readWriteLock +
                '}';
    }
}
