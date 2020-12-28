package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Logger;

import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;
import com.sap.sse.security.subscription.SubscriptionCancelResult;
import com.sap.sse.security.subscription.SubscriptionRequestManagementService;

/**
 * Task to perform canceling a subscription by subscription id
 */
public class ChargebeeCancelSubscriptionTask
        implements ChargebeeSubscriptionRequest.OnResultListener, ChargebeeCancelSubscriptionRequest.OnResultListener {
    private static final Logger logger = Logger.getLogger(ChargebeeCancelSubscriptionTask.class.getName());

    /**
     * Cancel result listener
     */
    public static interface OnResultListener {
        void onCancelResult(SubscriptionCancelResult result);
    }

    private final String subscriptionId;
    private final SubscriptionRequestManagementService requestManagementService;
    private OnResultListener listener;

    public ChargebeeCancelSubscriptionTask(String subscriptionId,
            SubscriptionRequestManagementService requestManagementService, OnResultListener listener) {
        this.subscriptionId = subscriptionId;
        this.requestManagementService = requestManagementService;
        this.listener = listener;
    }

    public void run() {
        logger.info(() -> "Schedule cancel Chargebee subscription, id: " + subscriptionId);
        requestManagementService.scheduleRequest(new ChargebeeSubscriptionRequest(subscriptionId, this),
                ChargebeeApiService.TIME_FOR_API_REQUEST_MS, ChargebeeApiService.LIMIT_REACHED_RESUME_DELAY_MS);
    }

    @Override
    public void onSubscriptionResult(com.chargebee.models.Subscription subscription) {
        if (subscription != null) {
            Subscription sub = new ChargebeeApiSubscriptionData(subscription, /* invoice */null, /* transaction */ null)
                    .toSubscription();
            String status = subscription.status().name();
            if (status != null) {
                status = status.toLowerCase();
            }
            if (sub.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED)) {
                onDone(new SubscriptionCancelResult(/* success */ true, sub));
            } else {
                requestManagementService.scheduleRequest(new ChargebeeCancelSubscriptionRequest(subscriptionId, this),
                        ChargebeeApiService.TIME_FOR_API_REQUEST_MS, ChargebeeApiService.LIMIT_REACHED_RESUME_DELAY_MS);
            }
        } else {
            onDone(new SubscriptionCancelResult(/* success */false, /* subscription */null, /* deleted */true));
        }
    }

    @Override
    public void onSubscriptionCancelResult(com.chargebee.models.Subscription subscription) {
        if (subscription != null) {
            Subscription sub = (new ChargebeeApiSubscriptionData(subscription, null, null)).toSubscription();
            boolean success = sub.getSubscriptionStatus().equals(ChargebeeSubscription.SUBSCRIPTION_STATUS_CANCELLED);
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
