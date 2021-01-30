package com.sap.sse.security.subscription.chargebee;

import com.chargebee.Environment;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscriptionProvider;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;
import com.sap.sse.security.subscription.SubscriptionApiService;
import com.sap.sse.security.subscription.SubscriptionDataHandler;

public class ChargebeeApiService implements SubscriptionApiService {
    private final boolean active;

    private final SubscriptionApiRequestProcessor requestProcessor;

    public ChargebeeApiService(ChargebeeConfiguration configuration, SubscriptionApiRequestProcessor requestProcessor) {
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
    public void getUserSubscriptions(User user, OnSubscriptionsResultListener listener) {
        new ChargebeeFetchUserSubscriptionsTask(user, requestProcessor,
                subscriptions -> listener.onSubscriptionsResult(user, subscriptions)).run();
    }

    @Override
    public void cancelSubscription(String subscriptionId, OnCancelSubscriptionResultListener listener) {
        new ChargebeeCancelSubscriptionTask(subscriptionId, requestProcessor, result -> listener.onCancelResult(result))
                .run();
    }

    @Override
    public SubscriptionDataHandler getDataHandler() {
        return new ChargebeeSubscriptionDataHandler();
    }
}
