# Titan Cache Balance Limiter-LLD is a robust rate limiting solution designed to handle high traffic scenarios with fine-grained control over API request rates.
  Built with a variety of rate limiting algorithms, this project provides flexible and scalable options for managing user requests, preventing abuse, 
  and ensuring system stability.

# Multiple Rate Limiting Algorithms:

* Token Bucket: Allows requests to be handled in bursts and refills tokens over time.
* Leaky Bucket: Ensures a steady flow of requests by enforcing a fixed rate of request processing.
* Fixed Window Counter: Counts requests in fixed time windows and limits based on predefined thresholds.
* Sliding Window Log: Logs timestamps of requests and checks their frequency within a sliding window to allow or deny requests.
* Sliding Window Counter: A hybrid approach combining fixed window and sliding window log techniques to smooth out traffic spikes.
* Exponential Moving Average (EMA) Bucket: Dynamically adjusts rate limits based on moving averages to adapt to varying traffic patterns.
* Burst Rate Limiting: Manages high burst traffic by allowing a temporary capacity to absorb spikes while maintaining overall rate limits.
* Adaptive Rate Limiting: Adjusts rate limits in real-time based on traffic patterns, allowing the system to adapt to changing usage.
* Rate Limiting with Quotas: Sets predefined quotas for different users or services, ensuring fair usage distribution.
* EMABucket Rate Limiting: Utilizes the Exponential Moving Average to smooth out traffic patterns and apply adaptive rate limits.
* Circuit Breaker Rate Limiting: Protects your services from overload by breaking the circuit when the request rate exceeds a safe threshold.
* Testing: Includes comprehensive JUnit test suites for both small and large datasets to ensure robust performance and correctness under various scenarios.

# Project Structure:
  The project follows the Model-View-Controller (MVC) design pattern:

  * Model (titanVault.model): Contains classes representing different rate limiting strategies and configurations, such as EMABucket, LeakyBucket, TokenBucket, and more.

  * Service (titanVault.service): Implements the business logic for rate limiting, including classes like EMABucketRateLimiter, LeakyBucketRateLimiter, BurstRateServiceLimiter, etc.

  * Controller (titanVault.controller): Manages incoming requests and interacts with the service layer to apply rate limiting policies.

  * Test (titanVault.serviceTest): Contains unit tests for rate limiting algorithms with two categories:

    Small Dataset Tests: Tests with moderate values to ensure correctness.
    Large Dataset Tests: Tests with large values to stress-test the system.

# Installation:
  Prerequisites
  Java 11 or higher
  Maven 3.6 or higher

# Clone the Repository:
bash
Copy code
git clone https://github.com/dipayan-code97/titan-cache-balance-limiter-LLD.git
cd titan-cache-balance-limiter-LLD

# mvn clean install
Running Tests
Run the JUnit tests with Maven:
mvn test

License
This project is licensed under the MIT License. See the LICENSE file for details.

Contact
For any questions or suggestions, please open an issue or reach out to the maintainers via email at your.email@example.com.

