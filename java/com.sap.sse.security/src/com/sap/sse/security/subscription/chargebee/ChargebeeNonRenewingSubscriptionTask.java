package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Logger;

import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.subscription.SubscriptionApiBaseService;
import com.sap.sse.security.subscription.SubscriptionApiRequestProcessor;
import com.sap.sse.security.subscription.SubscriptionNonRenewingResult;

/**
 * Task to perform a non renewing action of a subscription by subscription id
 */
public class ChargebeeNonRenewingSubscriptionTask
        implements ChargebeeSubscriptionRequest.OnResultListener, ChargebeeNonRenewingSubscriptionRequest.OnResultListener {
    private static final Logger logger = Logger.getLogger(ChargebeeNonRenewingSubscriptionTask.class.getName());

    /**
     * Cancel result listener
     */
    @FunctionalInterface
    public static interface OnResultListener {
        void onNonRenewingResult(SubscriptionNonRenewingResult result);
    }

    private final String subscriptionId;
    private final SubscriptionApiRequestProcessor requestProcessor;
    private final OnResultListener listener;
    private final SubscriptionApiBaseService chargebeeApiServiceParams;

    public ChargebeeNonRenewingSubscriptionTask(String subscriptionId, SubscriptionApiRequestProcessor requestProcessor,
            OnResultListener listener, SubscriptionApiBaseService chargebeeApiServiceParams) {
        this.subscriptionId = subscriptionId;
        this.requestProcessor = requestProcessor;
        this.listener = listener;
        this.chargebeeApiServiceParams = chargebeeApiServiceParams;
    }

    public void run() {
        logger.info(() -> "Schedule setting Chargebee subscription to non renewing, id: " + subscriptionId);
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
            if (sub.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_NON_RENEWING)) {
                onDone(new SubscriptionNonRenewingResult(/* success */ true, sub));
            } else {
                requestProcessor.addRequest(new ChargebeeNonRenewingSubscriptionRequest(subscriptionId, this,
                        requestProcessor, chargebeeApiServiceParams));
            }
        } else {
            onDone(new SubscriptionNonRenewingResult(/* success */false, /* subscription */null, /* deleted */true));
        }
    }

    @Override
    public void onSubscriptionNonRenewingResult(com.chargebee.models.Subscription subscription) {
        if (subscription != null) {
            Subscription sub = (new ChargebeeApiSubscriptionData(subscription, null, null))
                    .toSubscription(chargebeeApiServiceParams.getSubscriptionPlanProvider());
            boolean success = sub.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_NON_RENEWING);
            onDone(new SubscriptionNonRenewingResult(success, sub, /* deleted */ false));
        } else {
            onDone(new SubscriptionNonRenewingResult(/* success */false, /* subscription */null, /* deleted */true));
        }
    }

    private void onDone(SubscriptionNonRenewingResult result) {
        if (listener != null) {
            listener.onNonRenewingResult(result);
        }
    }
}
