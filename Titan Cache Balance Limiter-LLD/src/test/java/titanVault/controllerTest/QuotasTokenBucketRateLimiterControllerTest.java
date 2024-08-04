package titanVault.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import titanVault.controller.QuotasTokenBucketRateLimiterController;
import titanVault.service.QuotasTokenBucketRateServiceLimiter;

import java.time.Clock;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuotasTokenBucketRateLimiterControllerTest {

    private QuotasTokenBucketRateServiceLimiter stubRateLimiterService;
    private QuotasTokenBucketRateLimiterController controller;

    @BeforeEach
    void setUp() {
        // Create a stub or mock for the rate limiter service
        stubRateLimiterService = new QuotasTokenBucketRateServiceLimiter(
                100, Duration.ofMinutes(1), 10, Clock.systemUTC(), 500, Duration.ofHours(1)
        );

        // Create the controller with the stub service
        controller = new QuotasTokenBucketRateLimiterController(
                100, Duration.ofMinutes(1), 10, Clock.systemUTC(), 500, Duration.ofHours(1)
        );
    }

    /* Positive test case for handling a request when allowed */
    @Test
    void testHandleRequestAllowed() {
        String userId = "user1";
        long requestAmount = 5;

        // Assume the rate limiter service allows this request
        assertDoesNotThrow(() -> {
            String result = controller.handleRequest(userId, requestAmount);
            assertEquals("Request allowed for user: User One", result);
        });
    }

    /* Negative test case for handling a request when denied */
    @Test
    void testHandleRequestDenied() {
        String userId = "user1";
        long requestAmount = 500; // Assuming the request exceeds the available quota

        // Setup the stub to deny this request
        // Note: You need to adapt this to match how the actual service is being setup
        stubRateLimiterService = new QuotasTokenBucketRateServiceLimiter(
                100, Duration.ofMinutes(1), 10, Clock.systemUTC(), 0, Duration.ofHours(1)
        );
        controller = new QuotasTokenBucketRateLimiterController(
                100, Duration.ofMinutes(1), 10, Clock.systemUTC(), 0, Duration.ofHours(1)
        );

        // Execute and verify the denial
        String result = controller.handleRequest(userId, requestAmount);
        assertEquals("Request denied for user: User One", result);
    }

    /* Negative test case for handling a request with an invalid userId */
    @Test
    void testHandleRequestInvalidUserId() {
        String userId = "";
        long requestAmount = 5;

        // Execute and verify that the userId is handled correctly
        String result = controller.handleRequest(userId, requestAmount);
        assertEquals("Request denied for user: Unknown User", result);
    }

    /* Positive test case for getting the remaining quota */
    @Test
    void testGetRemainingQuota() {
        String userId = "user1";

        // Assume the rate limiter service returns a certain remaining quota
        // This should be adapted to match the service's behavior
        long remainingQuota = 300; // Example quota value
        String result = controller.getRemainingQuota(userId);
        assertEquals("Remaining quota for user User One: " + remainingQuota, result);
    }

    /* Negative test case for getting remaining quota with an invalid userId */
    @Test
    void testGetRemainingQuotaInvalidUserId() {
        String userId = "";

        // Assume the rate limiter service returns a certain remaining quota
        long remainingQuota = 0; // Example quota value
        String result = controller.getRemainingQuota(userId);
        assertEquals("Remaining quota for user Unknown User: " + remainingQuota, result);
    }
}
