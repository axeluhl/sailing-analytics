package com.sap.sse.security.ui.shared.subscription.chargebee;

import com.sap.sse.common.TimePoint;
import com.sap.sse.security.ui.client.subscription.chargebee.ChargebeeSubscriptionClientProvider;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;

public class ChargebeeSubscriptionDTO extends SubscriptionDTO {
    public static final String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static final String SUBSCRIPTION_STATUS_PAUSED = "paused";
    public static final String TRANSACTION_TYPE_REFUND = "refund";

    public ChargebeeSubscriptionDTO() {
    }

    public ChargebeeSubscriptionDTO(String planId, TimePoint trialStart, TimePoint trialEnd, String subscriptionStatus,
            String paymentStatus, String transactionType) {
        super(planId, trialStart, trialEnd, subscriptionStatus, paymentStatus, transactionType,
                ChargebeeSubscriptionClientProvider.PROVIDER_NAME);
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
    public boolean isRefunded() {
        return getTransactionType() != null && getTransactionType().equals(TRANSACTION_TYPE_REFUND);
    }

    @Override
    public boolean isPaused() {
        return getSubscriptionStatus() != null && getSubscriptionStatus().equals(SUBSCRIPTION_STATUS_PAUSED);
    }
}
