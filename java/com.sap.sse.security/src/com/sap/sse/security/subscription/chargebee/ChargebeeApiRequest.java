package com.sap.sse.security.subscription.chargebee;

import com.chargebee.ApiResponse;
import com.sap.sse.security.subscription.AbstractSubscriptionApiRequest;

/**
 * Base implementation for Chargebee's API requests
 */
public abstract class ChargebeeApiRequest extends AbstractSubscriptionApiRequest {
    protected boolean needResume;

    @Override
    public boolean needResume() {
        return needResume;
    }

    protected boolean isRateLimitReached(ApiResponse response) {
        needResume = response.httpCode() == 429;
        return needResume;
    }
}
