package com.sap.sse.security.ui.shared.subscription;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.subscription.Subscription;

public abstract class SubscriptionDTO implements HasSubscriptionMessageKeys, IsSerializable {

    protected static final String PAYMENT_STATUS_SUCCESS = "success";
    private static final String PAYMENT_STATUS_NO_SUCCESS = "no_success";

    /**
     * User current subscription plan id
     */
    private String planId;

    private String subscriptionId;

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

    /*
     * The Value of the reocurring payment in cents. Depends on the currency code.
     */
    private Integer reoccuringPaymentValue;

    /**
     * The currency code for the {@link #reoccuringPaymentValue recurring payment value}
     */
    private String currencyCode;

    /**
     * Point in time when the subscription has originally been created.
     */
    private TimePoint createdAt;

    /*
     * TIme at which the subscription status was last changed to cancelled or will be changed to cancelled,
     * if it is planned for cancellation.
     */
    private TimePoint cancelledAt;

    /**
     * The date/time at which the next billing for the subscription happens.
     * This is usually right after current_term_end unless multiple subscription terms
     * were invoiced in advance using the terms_to_charge parameter.
     * optional
     */

    private TimePoint nextBillingAt;

    /**
     * End of the current billing period of the subscription. Subscription is renewed immediately after this.
     * optional
     */
    private TimePoint currentTermEnd;

    /**
     * End of the current trial period of the subscription. Subscription is billed immediately after this.
     * optional
     */
    private TimePoint trialEnd;

    /**
     * Subscription provider name
     */
    private String provider;

    protected SubscriptionDTO() {}

    protected SubscriptionDTO(final String planId, final String subscriptionId, final String subscriptionStatus,
            final String paymentStatus, final String transactionType, final Integer reoccuringPaymentValue,
            final String currencyCode, final TimePoint createdAt, final TimePoint trialEnd,
            final TimePoint currentTermEnd, final TimePoint cancelledAt, final TimePoint nextBillingAt,
            final String provider) {
        this.planId = planId;
        this.subscriptionId = subscriptionId;
        this.trialEnd = trialEnd;
        this.subscriptionStatus = subscriptionStatus;
        this.paymentStatus = paymentStatus;
        this.transactionType = transactionType;
        this.provider = provider;
        this.reoccuringPaymentValue = reoccuringPaymentValue;
        this.currencyCode = currencyCode;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
        this.nextBillingAt = nextBillingAt;
        this.currentTermEnd = currentTermEnd;
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
     * Check if subscription is in cancelled status
     */
    public abstract boolean isCancelled();

    /**
     * Check if subscription transaction is refunded
     */
    public abstract boolean isRefunded();

    /**
     * Check if {@link #nextBillingAt} and {@link #reoccuringPaymentValue} are available
     */
    public boolean isRenewing() {
        return nextBillingAt != null && !Subscription.emptyTime().equals(nextBillingAt) && reoccuringPaymentValue != null;
    }

    /**
     * Returns {@code true} if payment status is successful, {@code false} otherwise
     */
    public boolean isPaymentSuccess() {
        return PAYMENT_STATUS_SUCCESS.equals(paymentStatus);
    }

    /**
     * Returns {@code true} if payment status is not successful, {@code false} otherwise
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

    public TimePoint getTrialEnd() {
        return trialEnd;
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

    public Integer getReoccuringPaymentValue() {
        return reoccuringPaymentValue;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public TimePoint getCreatedAt() {
        return createdAt;
    }

    public TimePoint getCancelledAt() {
        return cancelledAt;
    }

    public TimePoint getNextBillingAt() {
        return nextBillingAt;
    }

    public TimePoint getCurrentTermEnd() {
        return currentTermEnd;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

}
