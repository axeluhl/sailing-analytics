package com.sap.sse.security.subscription.chargebee;

import static com.chargebee.models.Subscription.cancel;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.Result;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.CancelRequest;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;

/**
 * Cancel Chargebee subscription request
 */
public class ChargebeeCancelSubscriptionRequest extends ChargebeeApiRequest {
    private static final Logger logger = Logger.getLogger(ChargebeeCancelSubscriptionRequest.class.getName());

    public static interface OnResultListener {
        void onSubscriptionCancelResult(Subscription subscription);
    }

    private final String subscriptionId;
    private final OnResultListener listener;

    public ChargebeeCancelSubscriptionRequest(String subscriptionId, OnResultListener listener,
            SubscriptionApiRequestProcessor requestProcessor) {
        super(requestProcessor);
        this.subscriptionId = subscriptionId;
        this.listener = listener;
    }

    @Override
    protected ChargebeeInternalApiRequestWrapper createRequest() {
        logger.info(() -> "Cancel Chargebee subscription, subscription id: " + subscriptionId);
        CancelRequest request = cancel(subscriptionId);
        return new ChargebeeInternalApiRequestWrapper(request);
    }

    @Override
    protected void processResult(ChargebeeInternalApiRequestWrapper request) {
        Result result = request.getResult();
        if (result != null) {
            onDone(result.subscription());
        } else {
            onDone(null);
        }
    }

    @Override
    protected void handleError(Exception e) {
        logger.log(Level.SEVERE, "Cancel Chargebee subscription failed, subscription id: " + subscriptionId, e);
        onDone(null);
    }

    private void onDone(Subscription subscription) {
        if (listener != null) {
            listener.onSubscriptionCancelResult(subscription);
        }
    }
}
