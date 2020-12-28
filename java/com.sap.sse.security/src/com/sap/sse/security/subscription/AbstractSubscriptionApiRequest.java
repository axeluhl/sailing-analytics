package com.sap.sse.security.subscription;

/**
 * Base subscription API request implementation for {@code SubscriptionApiRequest}. Concrete implementation should
 * extend from this class instead of implementing interface {@code SubscriptionApiRequest}
 */
public abstract class AbstractSubscriptionApiRequest implements SubscriptionApiRequest {
    private SubscriptionRequestManagementService requestManagementService;

    @Override
    public void setRequestManagementService(SubscriptionRequestManagementService requestManagementService) {
        this.requestManagementService = requestManagementService;
    }

    protected SubscriptionRequestManagementService getRequestManagementService() {
        return requestManagementService;
    }
}
