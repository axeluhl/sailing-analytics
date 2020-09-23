package com.sap.sailing.gwt.ui.shared.subscription.chargebee;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionItem;
import com.sap.sse.common.TimePoint;

public class ChargebeeSubscriptionItem extends SubscriptionItem {
    public static final String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static final String SUBSCRIPTION_STATUS_PAUSED = "paused";

    public static final String TRANSACTION_TYPE_REFUND = "refund";

    public ChargebeeSubscriptionItem() {
    }

    public ChargebeeSubscriptionItem(String planId, TimePoint trialStart, TimePoint trialEnd, String subscriptionStatus,
            String paymentStatus, String transactionType) {
        super(planId, trialStart, trialEnd, subscriptionStatus, paymentStatus, transactionType);
    }

    @Override
    public boolean isInTrial() {
        return getSubscriptionStatus() != null && getSubscriptionStatus().equals(SUBSCRIPTION_STATUS_TRIAL);
    }

    @Override
    public boolean isActive() {
        return getSubscriptionStatus() != null && getSubscriptionStatus().equals(SUBSCRIPTION_STATUS_ACTIVE);
    }

    @Override
    public String getSubscriptionStatusLabel() {
        final String label;
        final String subscriptionStatus = getSubscriptionStatus();
        if (subscriptionStatus != null) {
            if (subscriptionStatus.equals(SUBSCRIPTION_STATUS_TRIAL)) {
                label = StringMessages.INSTANCE.inTrial();
            } else if (subscriptionStatus.equals(SUBSCRIPTION_STATUS_ACTIVE)) {
                label = StringMessages.INSTANCE.active();
            } else if (subscriptionStatus.equals(SUBSCRIPTION_STATUS_PAUSED)) {
                label = StringMessages.INSTANCE.paused();
            } else {
                label = "";
            }
        } else {
            label = "";
        }
        return label;
    }

    @Override
    public String getPaymentStatusLabel() {
        final String label;
        final String paymentStatus = getPaymentStatus();
        if (paymentStatus != null) {
            if (isPaymentSuccess()) {
                if (isRefunded()) {
                    label = StringMessages.INSTANCE.refunded();
                } else {
                    label = StringMessages.INSTANCE.paymentStatusSuccess();
                }
            } else {
                label = StringMessages.INSTANCE.paymentStatusNoSuccess();
            }
        } else {
            label = "";
        }
        return label;
    }

    @Override
    public boolean isRefunded() {
        return getTransactionType() != null && getTransactionType().equals(TRANSACTION_TYPE_REFUND);
    }

}
