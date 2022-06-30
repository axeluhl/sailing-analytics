package com.sap.sse.security.subscription.chargebee;

import com.chargebee.Environment;
import com.sap.sse.common.Duration;
import com.sap.sse.security.shared.SubscriptionPlanProvider;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscriptionProvider;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;
import com.sap.sse.security.subscription.SubscriptionApiService;
import com.sap.sse.security.subscription.SubscriptionDataHandler;

public class ChargebeeApiService implements SubscriptionApiService {
    private final boolean active;

    private final SubscriptionApiRequestProcessor requestProcessor;
    
    /**
     * Chargebee has API rate limits Threshold value for test site: ~750 API calls in 5 minutes. Threshold value for
     * live site: ~150 API calls per site per minute. So to prevent the limit would be reached, a request has a frame of
     * ~400ms, and a next request should be made after 400ms from previous request.
     */
    private static final Duration TIME_BETWEEN_API_REQUEST_START = Duration.ONE_MILLISECOND.times(400);

    /**
     * The delay after which to re-schedule a request that failed for having exceeded the service's rate limit
     */
    private static final Duration LIMIT_REACHED_RESUME_DELAY = Duration.ONE_MILLISECOND.times(65000);

    protected final SubscriptionPlanProvider subscriptionPlanProvider;

    public ChargebeeApiService(ChargebeeConfiguration configuration, SubscriptionApiRequestProcessor requestProcessor,
            SubscriptionPlanProvider subscriptionPlanProvider) {
        this.subscriptionPlanProvider = subscriptionPlanProvider;
        if (configuration != null) {
            Environment.configure(configuration.getSite(), configuration.getApiKey());
            active = true;
        } else {
            active = false;
        }
        this.requestProcessor = requestProcessor;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getProviderName() {
        return ChargebeeSubscriptionProvider.PROVIDER_NAME;
    }

    @Override
    public Duration getTimeBetweenApiRequestStart() {
        return TIME_BETWEEN_API_REQUEST_START;
    }

    @Override
    public Duration getLimitReachedResumeDelay() {
        return LIMIT_REACHED_RESUME_DELAY;
    }

    @Override
    public void getUserSubscriptions(OnSubscriptionsResultListener listener) {
        new ChargebeeFetchUserSubscriptionsTask(requestProcessor,
                subscriptions -> listener.onSubscriptionsResult(subscriptions), this).run();
    }

    @Override
    public void cancelSubscription(String subscriptionId, OnCancelSubscriptionResultListener listener) {
        new ChargebeeCancelSubscriptionTask(subscriptionId, requestProcessor, result -> listener.onCancelResult(result),
                this).run();
    }

    @Override
    public void getUserSelfServicePortalSession(String userId, OnSelfServicePortalSessionResultListener listener) {
        new ChargeBeeGetSelfServicePortalSessionTask(userId, requestProcessor,
                result -> listener.onSessionResult(result), this).run();
    }

    @Override
    public SubscriptionDataHandler getDataHandler() {
        return new ChargebeeSubscriptionDataHandler();
    }

    @Override
    public SubscriptionPlanProvider getSubscriptionPlanProvider() {
        return subscriptionPlanProvider;
    }

    @Override
    public void getItemPrices(OnItemPriceResultListener listener) {
        new ChargebeeFetchItemPricesTask(requestProcessor, result -> listener.onItemPriceResult(result), this).run();
    }
    
}
