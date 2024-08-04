package titanVault.service;

/**
 * Interface for a rate limiter to determine if a request is allowed.
 */
public interface RateLimiter {

    /**
     * Validate if a request is allowed based on the rate limiting policy.
     *
     * @param userId The user identifier.
     * @return true if the request is allowed, false otherwise.
     */
    boolean validateRequest(String userId);
}

