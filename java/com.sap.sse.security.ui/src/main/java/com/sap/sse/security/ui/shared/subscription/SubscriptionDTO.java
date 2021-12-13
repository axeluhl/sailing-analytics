package com.sap.sse.security.ui.shared.subscription;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public abstract class SubscriptionDTO implements HasSubscriptionMessageKeys, IsSerializable {

    protected static final String PAYMENT_STATUS_SUCCESS = "success";
    private static final String PAYMENT_STATUS_NO_SUCCESS = "no_success";

    /**
     * User current subscription plan id
     */
    private String planId;

    /**
     * Subscription status: active or trial
     */
    private String subscriptionStatus;

    /**
     * Subscription payment status: {@link #PAYMENT_STATUS_SUCCESS} or {@link #PAYMENT_STATUS_NO_SUCCESS}
     */
    private String paymentStatus;

    /**
     * Subscription transaction type: payment or refund
     */
    private String transactionType;

    /**
     * Subscription provider name
     */
    private String provider;

    private TimePoint currentEnd;
    private TimePoint createdAt;

    protected SubscriptionDTO() {
    }

    protected SubscriptionDTO(final String planId, final TimePoint startedAt, final TimePoint currentEnd,
            final String subscriptionStatus, final String paymentStatus, final String transactionType,
            final String provider) {
        this.planId = planId;
        this.createdAt = startedAt;
        this.currentEnd = currentEnd;
        this.subscriptionStatus = subscriptionStatus;
        this.paymentStatus = paymentStatus;
        this.transactionType = transactionType;
        this.provider = provider;
    }

    /**
     * Check if subscription is in trial status
     */
    public abstract boolean isInTrial();

    /**
     * Check if subscription is in active status
     */
    public abstract boolean isActive();

    /**
     * Check if subscription is in paused status
     */
    public abstract boolean isPaused();

    /**
     * Check if subscription transaction is refunded
     */
    public abstract boolean isRefunded();

    /**
     * Returns {@code true} if subscription is active and payment status is successful, {@code false} otherwise
     */
    public boolean isPaymentSuccess() {
        return PAYMENT_STATUS_SUCCESS.equals(paymentStatus);
    }

    /**
     * Returns {@code true} if subscription is active but payment was not successful, {@code false} otherwise
     */
    public boolean isPaymentNoSuccess() {
        return PAYMENT_STATUS_NO_SUCCESS.equals(paymentStatus);
    }

    @Override
    public String getSubscriptionPlanId() {
        return planId;
    }

    public String getProvider() {
        return provider;
    }

    public TimePoint getCurrentEnd() {
        return currentEnd;
    }

    public TimePoint getCreatedAt() {
        return createdAt;
    }

    protected String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    protected String getPaymentStatus() {
        return paymentStatus;
    }

    protected String getTransactionType() {
        return transactionType;
    }

}
