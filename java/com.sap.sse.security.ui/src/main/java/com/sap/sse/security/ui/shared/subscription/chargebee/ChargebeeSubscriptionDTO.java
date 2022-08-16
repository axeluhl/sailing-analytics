package com.sap.sse.security.ui.shared.subscription.chargebee;

import com.sap.sse.common.TimePoint;
import com.sap.sse.security.ui.client.subscription.chargebee.ChargebeeSubscriptionClientProvider;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;

public class ChargebeeSubscriptionDTO extends SubscriptionDTO {

    private static final String SUBSCRIPTION_STATUS_TRIAL = "in_trial";
    private static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
    private static final String SUBSCRIPTION_STATUS_NON_RENEWING = "non_renewing";
    private static final String SUBSCRIPTION_STATUS_PAUSED = "paused";
    private static final String SUBSCRIPTION_STATUS_CANCELLED = "cancelled";
    private static final String TRANSACTION_TYPE_REFUND = "refund";

    protected ChargebeeSubscriptionDTO() {
    }

    public ChargebeeSubscriptionDTO(final String planId, final String subscriptionId, final String subscriptionStatus,
            final String paymentStatus, final String transactionType, final Integer reoccuringPaymentValue,
            final String currencyCode, final TimePoint createdAt, final TimePoint trialEnd,
            final TimePoint currentTermEnd, final TimePoint cancelledAt, final TimePoint nextBillingAt) {
        super(planId, subscriptionId, subscriptionStatus, paymentStatus, transactionType, reoccuringPaymentValue, currencyCode,
                createdAt, trialEnd, currentTermEnd, cancelledAt, nextBillingAt,
                ChargebeeSubscriptionClientProvider.PROVIDER_NAME);
    }

    @Override
    public boolean isInTrial() {
        return SUBSCRIPTION_STATUS_TRIAL.equals(getSubscriptionStatus());
    }

    @Override
    public boolean isActive() {
        return SUBSCRIPTION_STATUS_ACTIVE.equals(getSubscriptionStatus()) || SUBSCRIPTION_STATUS_NON_RENEWING.equals(getSubscriptionStatus());
    }

    @Override
    public boolean isPaused() {
        return SUBSCRIPTION_STATUS_PAUSED.equals(getSubscriptionStatus());
    }

    @Override
    public boolean isCancelled() {
        return SUBSCRIPTION_STATUS_CANCELLED.equals(getSubscriptionStatus());
    }

    @Override
    public boolean isRefunded() {
        return TRANSACTION_TYPE_REFUND.equals(getTransactionType());
    }
}
