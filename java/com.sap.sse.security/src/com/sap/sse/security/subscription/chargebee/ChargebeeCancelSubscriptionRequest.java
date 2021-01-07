package com.sap.sse.security.subscription.chargebee;

import static com.chargebee.models.Subscription.cancel;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chargebee.Result;
import com.chargebee.models.Subscription;
import com.chargebee.models.Subscription.CancelRequest;

/**
 * Cancel Chargebee subscription request
 */
public class ChargebeeCancelSubscriptionRequest extends ChargebeeApiRequest {
    private static final Logger logger = Logger.getLogger(ChargebeeCancelSubscriptionRequest.class.getName());

    public static interface OnResultListener {
        void onSubscriptionCancelResult(Subscription subscription);
    }

    private String subscriptionId;
    private OnResultListener listener;

    public ChargebeeCancelSubscriptionRequest(String subscriptionId, OnResultListener listener) {
        this.subscriptionId = subscriptionId;
        this.listener = listener;
    }

    @Override
    public void run() {
        logger.info(() -> "Cancel Chargebee subscription, subscription id: " + subscriptionId);
        CancelRequest request = cancel(subscriptionId);
        try {
            Result result = request.request();
            if (!isRateLimitReached(result)) {
                onDone(result.subscription());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cancel Chargebee subscription failed, subscription id: " + subscriptionId, e);
            onDone(null);
        }
    }

    private void onDone(Subscription subscription) {
        if (listener != null) {
            listener.onSubscriptionCancelResult(subscription);
        }
    }
}
