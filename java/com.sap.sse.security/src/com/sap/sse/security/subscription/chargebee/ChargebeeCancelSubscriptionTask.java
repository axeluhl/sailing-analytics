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
        implements ChargebeeCancelSubscriptionRequest.OnResultListener {
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
        requestProcessor.addRequest(new ChargebeeCancelSubscriptionRequest(subscriptionId, this, requestProcessor, chargebeeApiServiceParams));
    }

    @Override
    public void onSubscriptionCancelResult(com.chargebee.models.Subscription subscriptionModel) {
        if (subscriptionModel != null) {
            ChargebeeApiSubscriptionData apiSubscriptionData = new ChargebeeApiSubscriptionData(subscriptionModel, null, null);
            Subscription subscription = apiSubscriptionData.toSubscription(chargebeeApiServiceParams.getSubscriptionPlanProvider());
            boolean success = subscription.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED);
            onDone(new SubscriptionCancelResult(success, subscription, /* deleted */ false));
        } else {
            onDone(new SubscriptionCancelResult(/* success */false, /* subscription */null, /* deleted */true));
        }
    }

    private void onDone(SubscriptionCancelResult result) {
        logger.info("Subscription cancelation is done with success = " + result.isSuccess() + 
                ", deleted = " + result.isDeleted() + 
                ", subscription = " + result.getSubscription());
        if (listener != null) {
            listener.onCancelResult(result);
        }
    }
}
