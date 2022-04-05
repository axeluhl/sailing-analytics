package com.sap.sse.security.subscription.chargebee;

import com.chargebee.ApiResponse;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequest;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Base implementation for Chargebee's API requests. Implementations are expected to be able to {@link #createRequest()
 * create a request}, {@link #processResult(ChargebeeInternalApiRequestWrapper) process results}, and
 * {@link #handleError(Exception) handle errors that occur while executing the request}. This class provides the
 * {@link #run()} implementation which {@link #createRequest() creates the request},
 * {@link ChargebeeInternalApiRequestWrapper#request() executes the request}, checks whether the
 * {@link #isRateLimitReached(ApiResponse) API's rate limit is reached}, and if so, re-schedules the request with a
 * delay of {@link #LIMIT_REACHED_RESUME_DELAY_MS} milliseconds, or otherwise
 * {@link #processResult(ChargebeeInternalApiRequestWrapper) processes the results}.
 */
public abstract class ChargebeeApiRequest implements SubscriptionApiRequest {
    private final SubscriptionApiRequestProcessor requestProcessor;
    private final SubscriptionApiBaseService chargebeeApiServiceParams;

    protected ChargebeeApiRequest(SubscriptionApiRequestProcessor requestProcessor, SubscriptionApiBaseService chargebeeApiServiceParams) {
        this.requestProcessor = requestProcessor;
        this.chargebeeApiServiceParams = chargebeeApiServiceParams;
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
                    requestProcessor.rescheduleRequestAfterRateLimitExceeded(this);
                }
            } catch (Exception e) {
                handleError(e, () -> requestProcessor.rescheduleRequestAfterRateLimitExceeded(this));
            }
        }
    }
    
    protected abstract ChargebeeInternalApiRequestWrapper createRequest();

    @Override
    public SubscriptionApiBaseService getSubscriptionApiBaseService() {
        return chargebeeApiServiceParams;
    }

    protected abstract void processResult(ChargebeeInternalApiRequestWrapper request);

    protected abstract void handleError(Exception e, Runnable reschedule);

    protected SubscriptionApiRequestProcessor getRequestProcessor() {
        return requestProcessor;
    }

    private boolean isRateLimitReached(ApiResponse response) {
        return response.httpCode() == 429;
    }
}
