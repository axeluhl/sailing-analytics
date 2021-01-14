package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.ListResult;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.SubscriptionListRequest;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Request to fetch Chargebee subscription by subscription id
 */
public class ChargebeeSubscriptionRequest extends ChargebeeApiRequest {
    private static final Logger logger = Logger.getLogger(ChargebeeSubscriptionRequest.class.getName());

    public static interface OnResultListener {
        void onSubscriptionResult(Subscription subscription);
    }

    private final SubscriptionApiRequestProcessor requestProcessor;
    private final String subscriptionId;
    private final OnResultListener listener;

    public ChargebeeSubscriptionRequest(String subscriptionId, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor) {
        this.subscriptionId = subscriptionId;
        this.listener = listener;
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void run() {
        logger.info(() -> "Fetch Chargebee subscription, subscription id: " + subscriptionId);
        SubscriptionListRequest request = Subscription.list().limit(1).id().is(subscriptionId).includeDeleted(false);

        try {
            ListResult result = request.request();
            if (!isRateLimitReached(result)) {
                final Subscription subscription;
                if (result != null && !result.isEmpty()) {
                    subscription = result.get(0).subscription();
                } else {
                    subscription = null;
                }
                onDone(subscription);
            } else {
                requestProcessor.addRequest(this, LIMIT_REACHED_RESUME_DELAY_MS);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fetch Chargebee subscription failed, subscription id: " + subscriptionId, e);
            onDone(null);
        }

    }

    private void onDone(Subscription subscription) {
        if (listener != null) {
            listener.onSubscriptionResult(subscription);
        }
    }
}
