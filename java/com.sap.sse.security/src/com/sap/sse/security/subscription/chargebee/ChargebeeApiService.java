package com.sap.sse.security.subscription.chargebee;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.chargebee.Environment;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscriptionProvider;
import com.sap.sse.security.subscription.SubscriptionApiService;
import com.sap.sse.security.subscription.SubscriptionCancelResult;
import com.sap.sse.security.subscription.SubscriptionDataHandler;
import com.sap.sse.security.subscription.SubscriptionRequestManagementService;

public class ChargebeeApiService implements SubscriptionApiService {
    // Chargebee has API rate limits
    // Threshold value for test site: ~750 API calls in 5 minutes.
    // Threshold value for live site: ~150 API calls per site per minute.
    // So to prevent the limit would be reached, a request has a frame of ~400ms, and a next request should be made
    // after 400ms from previous request
    public static final long TIME_FOR_API_REQUEST_MS = 400;

    public static final long LIMIT_REACHED_RESUME_DELAY_MS = 65000;

    private final boolean active;

    private SubscriptionRequestManagementService requestManagementService;

    public ChargebeeApiService(ChargebeeConfiguration configuration,
            SubscriptionRequestManagementService requestManagementService) {
        if (configuration != null) {
            Environment.configure(configuration.getSite(), configuration.getApiKey());
            active = true;
        } else {
            active = false;
        }
        this.requestManagementService = requestManagementService;
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
    public Future<Iterable<Subscription>> getUserSubscriptions(User user) {
        CompletableFuture<Iterable<Subscription>> result = new CompletableFuture<Iterable<Subscription>>();
        new ChargebeeFetchUserSubscriptionsTask(user, requestManagementService,
                new ChargebeeFetchUserSubscriptionsTask.OnResultListener() {

                    @Override
                    public void onSubsctiptionsResult(Iterable<Subscription> subscriptions) {
                        result.complete(subscriptions);
                    }
                }).run();
        return result;
    }

    @Override
    public Future<SubscriptionCancelResult> cancelSubscription(String subscriptionId) {
        CompletableFuture<SubscriptionCancelResult> result = new CompletableFuture<SubscriptionCancelResult>();
        new ChargebeeCancelSubscriptionTask(subscriptionId, requestManagementService,
                new ChargebeeCancelSubscriptionTask.OnResultListener() {

                    @Override
                    public void onCancelResult(SubscriptionCancelResult r) {
                        result.complete(r);
                    }
                }).run();
        return result;
    }

    @Override
    public SubscriptionDataHandler getDataHandler() {
        return new ChargebeeSubscriptionDataHandler();
    }
}
