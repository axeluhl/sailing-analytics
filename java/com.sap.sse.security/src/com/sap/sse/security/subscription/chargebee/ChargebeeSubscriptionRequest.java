package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.ListResult;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.SubscriptionListRequest;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Request to fetch Chargebee subscription by subscription id
 */
public class ChargebeeSubscriptionRequest extends ChargebeeApiRequest {
    private static final Logger logger = Logger.getLogger(ChargebeeSubscriptionRequest.class.getName());

    public static interface OnResultListener {
        void onSubscriptionResult(Subscription subscription);
    }

    private final String subscriptionId;
    private final OnResultListener listener;

    public ChargebeeSubscriptionRequest(String subscriptionId, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor, SubscriptionApiBaseService chargebeeApiServiceParams) {
        super(requestProcessor, chargebeeApiServiceParams);
        this.subscriptionId = subscriptionId;
        this.listener = listener;
    }

    @Override
    protected ChargebeeInternalApiRequestWrapper createRequest() {
        logger.info(() -> "Fetch Chargebee subscription, subscription id: " + subscriptionId);
        SubscriptionListRequest request = Subscription.list().limit(1).id().is(subscriptionId).includeDeleted(false);
        return new ChargebeeInternalApiRequestWrapper(request);
    }

    @Override
    protected void processResult(ChargebeeInternalApiRequestWrapper request) {
        ListResult result = request.getListResult();
        final Subscription subscription;
        if (result != null && !result.isEmpty()) {
            subscription = result.get(0).subscription();
        } else {
            subscription = null;
        }
        onDone(subscription);
    }

    @Override
    protected void handleError(Exception e) {
        logger.log(Level.SEVERE, "Fetch Chargebee subscription failed, subscription id: " + subscriptionId, e);
        onDone(null);
    }

    private void onDone(Subscription subscription) {
        if (listener != null) {
            listener.onSubscriptionResult(subscription);
        }
    }
}
