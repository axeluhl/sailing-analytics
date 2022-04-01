package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Logger;

import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;
import com.sap.sse.security.subscription.SubscriptionCancelResult;

/**
 * Task to perform canceling a subscription by subscription id
 */
public class ChargebeeCancelSubscriptionTask
        implements ChargebeeSubscriptionRequest.OnResultListener, ChargebeeCancelSubscriptionRequest.OnResultListener {
    private static final Logger logger = Logger.getLogger(ChargebeeCancelSubscriptionTask.class.getName());

    /**
     * Cancel result listener
     */
    @FunctionalInterface
    public static interface OnResultListener {
        void onCancelResult(SubscriptionCancelResult result);
    }

    private final String subscriptionId;
    private final SubscriptionApiRequestProcessor requestProcessor;
    private final OnResultListener listener;
    private final SubscriptionApiBaseService chargebeeApiServiceParams;

    public ChargebeeCancelSubscriptionTask(String subscriptionId, SubscriptionApiRequestProcessor requestProcessor,
            OnResultListener listener, SubscriptionApiBaseService chargebeeApiServiceParams) {
        this.subscriptionId = subscriptionId;
        this.requestProcessor = requestProcessor;
        this.listener = listener;
        this.chargebeeApiServiceParams = chargebeeApiServiceParams;
    }

    public void run() {
        logger.info(() -> "Schedule cancel Chargebee subscription, id: " + subscriptionId);
        requestProcessor.addRequest(new ChargebeeSubscriptionRequest(subscriptionId, this, requestProcessor, chargebeeApiServiceParams));
    }

    @Override
    public void onSubscriptionResult(com.chargebee.models.Subscription subscription) {
        if (subscription != null) {
            Subscription sub = new ChargebeeApiSubscriptionData(subscription, /* invoice */null, /* transaction */ null)
                    .toSubscription(chargebeeApiServiceParams.getSubscriptionPlanProvider());
            String status = subscription.status().name();
            if (status != null) {
                status = status.toLowerCase();
            }
            if (sub.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED)
                    || sub.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_NON_RENEWING)) {
                onDone(new SubscriptionCancelResult(/* success */ true, sub));
            } else {
                requestProcessor.addRequest(new ChargebeeCancelSubscriptionRequest(subscriptionId, this,
                        requestProcessor, chargebeeApiServiceParams));
            }
        } else {
            onDone(new SubscriptionCancelResult(/* success */false, /* subscription */null, /* deleted */true));
        }
    }

    @Override
    public void onSubscriptionCancelResult(com.chargebee.models.Subscription subscription) {
        if (subscription != null) {
            Subscription sub = (new ChargebeeApiSubscriptionData(subscription, null, null))
                    .toSubscription(chargebeeApiServiceParams.getSubscriptionPlanProvider());
            boolean success = sub.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED)
                    || sub.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_NON_RENEWING);
            onDone(new SubscriptionCancelResult(success, sub, /* deleted */ false));
        } else {
            onDone(new SubscriptionCancelResult(/* success */false, /* subscription */null, /* deleted */true));
        }
    }

    private void onDone(SubscriptionCancelResult result) {
        if (listener != null) {
            listener.onCancelResult(result);
        }
    }
}
