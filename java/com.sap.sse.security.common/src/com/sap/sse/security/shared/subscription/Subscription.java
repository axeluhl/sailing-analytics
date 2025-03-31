package com.sap.sse.security.shared.subscription;

import java.io.Serializable;

import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.impl.User;

/**
 * Subscription data model for a {@link User} which is stored persistently together with the {@link User} object. See,
 * e.g., {@link User#setSubscriptions(Subscription[])}.
 * 
 * @author Tu Tran
 */
public abstract class Subscription implements Serializable {
    public static final String PAYMENT_STATUS_SUCCESS = "success";
    public static final String PAYMENT_STATUS_NO_SUCCESS = "no_success";

    public static TimePoint emptyTime() {
        return getTime(0);
    }

    public static TimePoint getTime(long milisTimestamp) {
        return TimePoint.of(milisTimestamp);
    }

    private static final long serialVersionUID = 96845123954667808L;

    /**
     * Subscription id
     */
    private final String subscriptionId;

    /**
     * Current subscription plan id
     */
    private final String planId;

    /**
     * Chargebee's customer id, taken from {@link User#getName()}
     */
    private final String customerId;

    /**
     * Subscription trial start time
     */
    private final TimePoint trialStart;

    /**
     * Subscription trial end time
     */
    private final TimePoint trialEnd;

    /**
     * Subscription status; from the Chargebee docs:
     * <p>
     * 
     * Possible values are:
     * <ul>
     * <li><tt>future</tt> The subscription is scheduled to start at a future date.</li>
     * <li><tt>in_trial</tt> The subscription is in trial.</li>
     * <li><tt>active</tt> The subscription is active and will be charged for automatically based on the items in
     * it.</li>
     * <li><tt>non_renewing</tt> The subscription will be canceled at the end of the current term.</li>
     * <li><tt>paused</tt> The subscription is paused. The subscription will not renew while in this state.</li>
     * <li><tt>cancelled</tt> The subscription has been canceled and is no longer in service.</li>
     * </ul>
     */
    private final String subscriptionStatus;

    /**
     * Subscription payment status, it records if user has successfully paid for the subscription. User will pay for the
     * subscription only if the subscription is turned to active (after trial period). If user has successfully paid for
     * the subscription, this has value {@code "success"}, otherwise {@code "no_success"}
     */
    protected String paymentStatus;

    /**
     * Subscription latest invoice id
     */
    protected String invoiceId;

    /**
     * Subscription latest invoice status
     */
    protected String invoiceStatus;

    /**
     * Subscription transaction type
     */
    protected String transactionType;

    /**
     * Subscription transaction status
     */
    protected String transactionStatus;

    /**
     * Record the creating time of the subscription
     */
    private final TimePoint subscriptionCreatedAt;
    
    /*
     * The Value of the reocurring payment in cents. Depends on the currency type.
     */
    private final Integer reoccuringPaymentValue;
    
    /*
     * The String representation of the currency code used 
     */
    private final String currencyCode;
    
    /*
     * TIme at which the subscription status was last changed to cancelled or will be changed to cancelled, 
     * if it is planned for cancellation.
     */
    private final TimePoint cancelledAt;
    
    /**
     * Time at which the subscription status last changed to  active. 
     * For example, this value is updated when an in_trial or  cancelled subscription activates.
     * optional
     */
    
    private final TimePoint subscriptionActivatedAt;
    
    /**
     * The date/time at which the next billing for the subscription happens. 
     * This is usually right after current_term_end unless multiple subscription terms 
     * were invoiced in advance using the terms_to_charge parameter. 
     * optional
     */
    private final TimePoint nextBillingAt;
    
    /**
     * End of the current billing period of the subscription. Subscription is renewed immediately after this.
     * optional
     */
    private final TimePoint currentTermEnd;

    /**
     * Record the updating time of the subscription
     */
    private final TimePoint subscriptionUpdatedAt;

    /**
     * Record the time of the latest handled WebHook event. Because a WebHook event might be retried to send to our
     * system if it has been failed on the previous time, so we will this time to only process a newer event, otherwise
     * we will update wrong data for a user.
     */
    private final TimePoint latestEventTime;

    /**
     * Record the time the subscription was updated by user using the system, like changing plan or cancel subscription.
     * Reason is payment service will retried to send us failed WebHook events, so with this time we'll process only
     * WebHook events occur after this time, otherwise we'll update our system with old data.
     */
    private final TimePoint manualUpdatedAt;

    /**
     * Subscription provider name
     */
    private final String providerName;

    public Subscription(String subscriptionId, String planId, String customerId, TimePoint trialStart,
            TimePoint trialEnd, String subscriptionStatus, String paymentStatus, String transactionType,
            String transactionStatus, String invoiceId, String invoiceStatus, Integer reoccuringPaymentValue,
            String currencyCode, TimePoint subscriptionCreatedAt, TimePoint subscriptionUpdatedAt,
            TimePoint subscriptionActivatedAt, TimePoint nextBillingAt, TimePoint currentTermEnd, TimePoint cancelledAt,
            TimePoint latestEventTime, TimePoint manualUpdatedAt, String providerName) {
        this.subscriptionId = subscriptionId;
        this.planId = planId;
        this.customerId = customerId;
        this.currencyCode = currencyCode;
        this.subscriptionActivatedAt = subscriptionActivatedAt;
        this.nextBillingAt = nextBillingAt;
        this.currentTermEnd = currentTermEnd;
        this.reoccuringPaymentValue = reoccuringPaymentValue;
        this.cancelledAt = cancelledAt;
        this.trialStart = trialStart == null ? emptyTime() : trialStart;
        this.trialEnd = trialEnd == null ? emptyTime() : trialEnd;
        this.subscriptionStatus = subscriptionStatus;
        this.paymentStatus = paymentStatus;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.invoiceId = invoiceId;
        this.invoiceStatus = invoiceStatus;
        this.subscriptionCreatedAt = subscriptionCreatedAt == null ? emptyTime() : subscriptionCreatedAt;
        this.subscriptionUpdatedAt = subscriptionUpdatedAt == null ? emptyTime() : subscriptionUpdatedAt;
        this.latestEventTime = latestEventTime == null ? emptyTime() : latestEventTime;
        this.manualUpdatedAt = manualUpdatedAt == null ? emptyTime() : manualUpdatedAt;
        this.providerName = providerName;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getPlanId() {
        return planId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public TimePoint getTrialStart() {
        return trialStart;
    }

    public TimePoint getTrialEnd() {
        return trialEnd;
    }

    /**
     * Returns the subscription status; from the Chargebee docs:
     * <p>
     * 
     * Possible values are:
     * <ul>
     * <li><tt>future</tt> The subscription is scheduled to start at a future date.</li>
     * <li><tt>in_trial</tt> The subscription is in trial.</li>
     * <li><tt>active</tt> The subscription is active and will be charged for automatically based on the items in
     * it.</li>
     * <li><tt>non_renewing</tt> The subscription will be canceled at the end of the current term.</li>
     * <li><tt>paused</tt> The subscription is paused. The subscription will not renew while in this state.</li>
     * <li><tt>cancelled</tt> The subscription has been canceled and is no longer in service.</li>
     * </ul>
     */
    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public TimePoint getSubscriptionCreatedAt() {
        return subscriptionCreatedAt;
    }

    public TimePoint getSubscriptionUpdatedAt() {
        return subscriptionUpdatedAt;
    }

    public TimePoint getLatestEventTime() {
        return latestEventTime;
    }

    public TimePoint getManualUpdatedAt() {
        return manualUpdatedAt;
    }

    public String getProviderName() {
        return providerName;
    }
    
    public TimePoint getSubscriptionActivatedAt() {
        return subscriptionActivatedAt;
    }

    public TimePoint getNextBillingAt() {
        return nextBillingAt;
    }

    public TimePoint getCurrentTermEnd() {
        return currentTermEnd;
    }

    public boolean hasPlan() {
        return planId != null && !planId.isEmpty();
    }

    public boolean hasSubscriptionId() {
        return subscriptionId != null && !subscriptionId.isEmpty();
    }

    /**
     * Check if subscription is active, base on this user will gain roles for the subscription
     * 
     * @return true if status subscription is in trial or status is active and user has success payment
     */
    public abstract boolean isActiveSubscription();
    
    /**
     * Check if subscription is updated more recently than other subscription
     */
    public boolean isUpdatedMoreRecently(Subscription otherSubscription) {
        return getManualUpdatedAt().asMillis() > otherSubscription.getManualUpdatedAt().asMillis()
                || getLatestEventTime().asMillis() > otherSubscription.getLatestEventTime().asMillis();
    }

    public TimePoint getCancelledAt() {
        return cancelledAt;
    }
    
    public Integer getReoccuringPaymentValue() {
        return reoccuringPaymentValue;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    /**
     * If the transaction data was not part of the returned api or webhook event, it is patched from the previous
     * subscription to retain information.
     * 
     * @param subscription
     */
    public abstract void patchTransactionData(Subscription subscription);

    /**
     * If the invoice data was not part of the returned api or webhook event, it is patched from the previous
     * subscription to retain information.
     * 
     * @param subscription
     */
    public abstract void patchInvoiceData(Subscription subscription);
    
    @Override
    public String toString() {
        return "Subscription [subscriptionId=" + subscriptionId + ", planId=" + planId + ", customerId=" + customerId
                + ", trialStart=" + trialStart + ", trialEnd=" + trialEnd + ", subscriptionStatus=" + subscriptionStatus
                + ", paymentStatus=" + paymentStatus + ", invoiceId=" + invoiceId + ", invoiceStatus=" + invoiceStatus
                + ", transactionType=" + transactionType + ", transactionStatus=" + transactionStatus
                + ", subscriptionCreatedAt=" + subscriptionCreatedAt + ", reoccuringPaymentValue="
                + reoccuringPaymentValue + ", currencyCode=" + getCurrencyCode() + ", cancelledAt=" + cancelledAt
                + ", subscriptionActivatedAt=" + subscriptionActivatedAt + ", nextBillingAt=" + nextBillingAt
                + ", currentTermEnd=" + currentTermEnd + ", subscriptionUpdatedAt=" + subscriptionUpdatedAt
                + ", latestEventTime=" + latestEventTime + ", manualUpdatedAt=" + manualUpdatedAt + ", providerName="
                + providerName + "]";
    }
}
