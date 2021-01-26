package com.sap.sse.security.subscription.chargebee;

import com.chargebee.ApiResponse;
import com.sap.sse.security.subscription.SubscriptionApiRequest;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Base implementation for Chargebee's API requests
 */
public abstract class ChargebeeApiRequest implements SubscriptionApiRequest {
    /**
     * Chargebee has API rate limits Threshold value for test site: ~750 API calls in 5 minutes. Threshold value for
     * live site: ~150 API calls per site per minute. So to prevent the limit would be reached, a request has a frame of
     * ~400ms, and a next request should be made after 400ms from previous request.
     */
    public static final long TIME_FOR_API_REQUEST_MS = 400;

    public static final long LIMIT_REACHED_RESUME_DELAY_MS = 65000;

    private final SubscriptionApiRequestProcessor requestProcessor;

    protected ChargebeeApiRequest(SubscriptionApiRequestProcessor requestProcessor) {
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void run() {
        ChargebeeInternalApiRequestWrapper request = createRequest();
        if (request != null) {
            try {
                request.request();
                if (!isRateLimitReached(request.getResponse())) {
                    processResult(request);
                } else {
                    requestProcessor.addRequest(this, LIMIT_REACHED_RESUME_DELAY_MS);
                }
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    protected abstract ChargebeeInternalApiRequestWrapper createRequest();

    protected abstract void processResult(ChargebeeInternalApiRequestWrapper request);

    protected abstract void handleError(Exception e);

    protected SubscriptionApiRequestProcessor getRequestProcessor() {
        return requestProcessor;
    }

    private boolean isRateLimitReached(ApiResponse response) {
        return response.httpCode() == 429;
    }
}
