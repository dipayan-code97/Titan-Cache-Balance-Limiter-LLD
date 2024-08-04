package titanVault.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.controller.CircuitBreakerRateLimiterController;
import titanVault.service.CircuitBreakerRateLimiter;

import static org.junit.jupiter.api.Assertions.*;

public class CircuitBreakerRateLimiterControllerTest {

    private CircuitBreakerRateLimiter circuitBreakerRateLimiter;
    private CircuitBreakerRateLimiterController circuitBreakerRateLimiterController;

    @BeforeEach
    void setUp() {
        // Create a stub or mock instance of CircuitBreakerRateLimiter service
        // Use reasonable parameters for testing
        circuitBreakerRateLimiter = new CircuitBreakerRateLimiter(10, 5, 3, 10000); // refillRate=10 tokens/sec, burstCapacity=5 tokens, failureThreshold=3, resetTimeoutMillis=10000
        // Initialize the controller with the stub service
        circuitBreakerRateLimiterController = new CircuitBreakerRateLimiterController(circuitBreakerRateLimiter);
    }

    /* Positive test case for handling a request when allowed */
    @Test
    void testHandleRequestAllowed() {
        String userId = "user123";

        // Execute the method
        boolean result = circuitBreakerRateLimiterController.handleRequest(userId);

        // Verify that the request is allowed
        assertTrue(result, "Request should be allowed when circuit breaker is closed and tokens are available.");
    }

    /* Negative test case for handling a request when denied due to rate limit or circuit breaker open */
    @Test
    void testHandleRequestDenied() {
        String userId = "user123";

        // Simulate failures to open the circuit breaker
        for (int i = 0; i < 3; i++) {
            circuitBreakerRateLimiterController.handleRequest(userId);
        }

        // Verify that the request is denied when the circuit breaker is OPEN or no tokens are available
        boolean result = circuitBreakerRateLimiterController.handleRequest(userId);
        assertFalse(result, "Request should be denied when the circuit breaker is open or tokens are depleted.");
    }

    /* Negative test case for handling a request with an invalid userId */
    @Test
    void testHandleRequestInvalidUserId() {
        String userId = "";

        // Verify that IllegalArgumentException is thrown
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                circuitBreakerRateLimiterController.handleRequest(userId)
        );

        // Verify the exception message
        assertEquals("User ID cannot be null or empty", thrown.getMessage());
    }

    /* Positive test case for getting the retry-after duration when rate limit is not exceeded */
    @Test
    void testGetRetryAfterWithTokens() {
        String userId = "user123";

        // Execute the method
        long retryAfter = circuitBreakerRateLimiterController.getRetryAfter(userId);

        // Verify that the retry-after duration is zero when tokens are available
        assertEquals(0, retryAfter, "Retry-after should be zero when tokens are available.");
    }

    /* Negative test case for getting retry-after with an invalid userId */
    @Test
    void testGetRetryAfterInvalidUserId() {
        String userId = "";

        // Verify that IllegalArgumentException is thrown
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                circuitBreakerRateLimiterController.getRetryAfter(userId)
        );

        // Verify the exception message
        assertEquals("User ID cannot be null or empty", thrown.getMessage());
    }
}
