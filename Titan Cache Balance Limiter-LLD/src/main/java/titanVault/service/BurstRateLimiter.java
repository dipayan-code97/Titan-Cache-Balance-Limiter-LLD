package titanVault.service;

public interface BurstRateLimiter {

    long getRetryAfter(String userId);
}
