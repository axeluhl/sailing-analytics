package com.sap.sse.security.subscription;

/**
 * Subscription provider API request interface. A request needs to be scheduled by
 * {@code SubscriptionRequestManagementService}
 */
public interface SubscriptionApiRequest {
    /**
     * Handle sending API request to subscription provider service
     */
    void run();

    /**
     * Whether this request needed to be resumed, such as in case APIs rate limit has been reached
     */
    boolean needResume();

    /**
     * Attach {@code SubscriptionRequestManagementService} instance to this request, so it could schedule sub-requests
     * if needed. An API request need to be scheduled to prevent provider's API request rate limits
     */
    void setRequestManagementService(SubscriptionRequestManagementService requestManagementService);
}
