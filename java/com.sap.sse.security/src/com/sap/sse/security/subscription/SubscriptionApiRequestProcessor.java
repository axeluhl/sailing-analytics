package com.sap.sse.security.subscription;

/**
 * Handle processing {@code SubscriptionApiRequest}. A request should be scheduled to perform in background executor,
 * and queued to prevent API request rate limit error
 */
public interface SubscriptionApiRequestProcessor {
    /**
     * Process requests {@code SubscriptionApiRequest}
     */
    void process();

    /**
     * Add request to this processor, and schedule it
     * 
     * @param delayMs
     *            delay time in milliseconds before start running the request, this is important to prevent API rate
     *            limit error
     */
    void addRequest(SubscriptionApiRequest request, long delayMs);
}
