package titanVault.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.controller.BurstRateLimiterController;
import titanVault.service.BurstRateServiceLimiter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for BurstRateLimiterController.
 */
class BurstRateLimiterControllerTest {

    private BurstRateServiceLimiter burstRateServiceLimiter;
    private BurstRateLimiterController controller;

    @BeforeEach
    void setUp() {
        /* Initialize the stub of the BurstRateServiceLimiter service */
        burstRateServiceLimiter = new BurstRateServiceLimiter(10, 5); // refillRate=10 tokens/sec, burstCapacity=5 tokens
        /* Create the controller with the stub service */
        controller = new BurstRateLimiterController(burstRateServiceLimiter);
    }

    /* Positive test case for handling a request when allowed */
    @Test
    void testHandleRequestAllowed() {
        String userId = "user123";

        /* Execute the method */
        boolean result = controller.handleRequest(userId);

        /* Verify that the request is allowed */
        assertTrue(result, "Request should be allowed when tokens are available.");
    }

    /* Negative test case for handling a request when denied */
    @Test
    void testHandleRequestDenied() {
        String userId = "user123";

        // Consume all tokens
        for (int i = 0; i < 5; i++) {
            controller.handleRequest(userId);
        }

        /* Execute the method */
        boolean result = controller.handleRequest(userId);

        /* Verify that the request is denied */
        assertFalse(result, "Request should be denied when no tokens are available.");
    }

    /* Negative test case for handling a request with an invalid userId */
    @Test
    void testHandleRequestInvalidUserId() {
        String userId = "";

        /* Verify that IllegalArgumentException is thrown */
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                controller.handleRequest(userId)
        );

        /* Verify the exception message */
        assertEquals("User ID cannot be null or empty", thrown.getMessage());
    }

    /* Positive test case for getting the retry-after duration when tokens are available */
    @Test
    void testGetRetryAfterWithTokens() {
        String userId = "user123";

        /* Execute the method */
        long retryAfter = controller.getRetryAfter(userId);

        /* Verify that the retry-after duration is zero when tokens are available */
        assertEquals(0, retryAfter, "Retry-after should be zero when tokens are available.");
    }

    /* Positive test case for getting the retry-after duration when tokens are depleted */
    @Test
    void testGetRetryAfterWithNoTokens() throws InterruptedException {
        String userId = "user123";

        // Consume all tokens
        for (int i = 0; i < 5; i++) {
            controller.handleRequest(userId);
        }

        // Wait for the refill thread to add tokens
        Thread.sleep(2000); // Wait for 2 seconds

        /* Execute the method */
        long retryAfter = controller.getRetryAfter(userId);

        /* Verify that the retry-after duration is zero after refill */
        assertEquals(0, retryAfter, "Retry-after should be zero when tokens are replenished.");
    }

    /* Negative test case for getting retry-after with an invalid userId */
    @Test
    void testGetRetryAfterInvalidUserId() {
        String userId = "";

        /* Verify that IllegalArgumentException is thrown */
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                controller.getRetryAfter(userId)
        );

        /* Verify the exception message */
        assertEquals("User ID cannot be null or empty", thrown.getMessage());
    }
}
