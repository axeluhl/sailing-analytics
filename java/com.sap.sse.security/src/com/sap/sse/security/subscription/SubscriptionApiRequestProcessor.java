package com.sap.sse.security.subscription;

/**
 * Handle processing {@code SubscriptionApiRequest}. A request should be scheduled to perform in background executor,
 * and queued to prevent API request rate limit error
 */
public interface SubscriptionApiRequestProcessor {
    /**
     * Add request to this processor and make sure it will be scheduled for execution as soon as possible,
     * respecting the service's rate limits.
     */
    void addRequest(SubscriptionApiRequest request);
    
    /**
     * When a request bounced due to the service's rate limit exceeded, this method can be called, passing the
     * request that failed, to re-schedule it after an appropriate delay.
     */
    void rescheduleRequestAfterRateLimitExceeded(SubscriptionApiRequest request);
}
