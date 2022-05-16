package com.sap.sse.security.subscription;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.subscription.Subscription;

/**
 * SubscriptionData holds subscription data map, and provides methods to retrieve common subscription data value
 * 
 */
public class SubscriptionData {
    public static enum DataAttribute {
        SUBSCRIPTION_ID,
        CUSTOMER_ID, PLAN_ID,
        TRIAL_START, TRIAL_END,
        SUBSCRIPTION_STATUS,
        PAYMENT_STATUS,
        TRANSACTION_TYPE,
        TRANSACTION_STATUS,
        INVOICE_ID,
        INVOICE_STATUS,
        SUBSCRIPTION_CREATED_AT,
        SUBSCRIPTION_UPDATED_AT,
        LATEST_EVENT_TIME,
        MANUAL_UPDATED_AT,
        PROVIDER_NAME,
        NEXT_BILLING_AT,
        CURRENT_TERM_END,
        CANCELLED_AT,
        SUBSCRIPTION_ACTIVATED_AT,
        REOCURRING_PAYMENT_VALUE,
        CURRENCY_CODE;
    }

    /**
     * Create empty subscription data instance that only holds plan id, latest webhook event update time, and manuall
     * updated time. This subscription is used for keeping updated time of subscription for a user, then it would
     * prevent issue of outdated data be persisted
     */
    public static SubscriptionData createEmptySubscriptionDataWithUpdateTimes(String planId, TimePoint latestEventTime,
            TimePoint manualUpdatedTime) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(DataAttribute.PLAN_ID.name(), planId);
        data.put(DataAttribute.LATEST_EVENT_TIME.name(), latestEventTime.asMillis());
        data.put(DataAttribute.MANUAL_UPDATED_AT.name(), manualUpdatedTime.asMillis());
        return new SubscriptionData(data);
    }

    private Map<String, Object> data;

    public SubscriptionData(Map<String, Object> data) {
        this.data = data;
    }
    
    public Map<String, Object> getMap() {
        return data;
    }

    public String getSubscriptionId() {
        return getMapStringValue(DataAttribute.SUBSCRIPTION_ID.name());
    }

    public String getPlanId() {
        return getMapStringValue(DataAttribute.PLAN_ID.name());
    }

    public String getCustomerId() {
        return getMapStringValue(DataAttribute.CUSTOMER_ID.name());
    }

    public TimePoint getTrialStart() {
        return getMapTimePointValue(DataAttribute.TRIAL_START.name());
    }

    public TimePoint getTrialEnd() {
        return getMapTimePointValue(DataAttribute.TRIAL_END.name());
    }

    public String getSubscriptionStatus() {
        return getMapStringValue(DataAttribute.SUBSCRIPTION_STATUS.name());
    }

    public String getPaymentStatus() {
        return getMapStringValue(DataAttribute.PAYMENT_STATUS.name());
    }

    public String getTransactionType() {
        return getMapStringValue(DataAttribute.TRANSACTION_TYPE.name());
    }

    public String getTransactionStatus() {
        return getMapStringValue(DataAttribute.TRANSACTION_STATUS.name());
    }

    public String getInvoiceId() {
        return getMapStringValue(DataAttribute.INVOICE_ID.name());
    }

    public String getInvoiceStatus() {
        return getMapStringValue(DataAttribute.INVOICE_STATUS.name());
    }

    public TimePoint getSubscriptionCreatedAt() {
        return getMapTimePointValue(DataAttribute.SUBSCRIPTION_CREATED_AT.name());
    }

    public TimePoint getSubscriptionUpdatedAt() {
        return getMapTimePointValue(DataAttribute.SUBSCRIPTION_UPDATED_AT.name());
    }

    public TimePoint getLatestEventTime() {
        return getMapTimePointValue(DataAttribute.LATEST_EVENT_TIME.name());
    }

    public TimePoint getManualUpdatedAt() {
        return getMapTimePointValue(DataAttribute.MANUAL_UPDATED_AT.name());
    }

    public String getProviderName() {
        return getMapStringValue(DataAttribute.PROVIDER_NAME.name());
    }
    
    public TimePoint getNextBillingAt() {
        return getMapTimePointValue(DataAttribute.NEXT_BILLING_AT.name());
    }
    
    public TimePoint getCurrentTermEnd() {
        return getMapTimePointValue(DataAttribute.CURRENT_TERM_END.name());
    }
    
    public TimePoint getCancelledAt() {
        return getMapTimePointValue(DataAttribute.CANCELLED_AT.name());
    }
    
    public TimePoint getSubscriptionActivatedAt() {
        return getMapTimePointValue(DataAttribute.SUBSCRIPTION_ACTIVATED_AT.name());
    }
    
    public Integer getReocurringPaymentValue() {
        return getMapIntegerValue(DataAttribute.REOCURRING_PAYMENT_VALUE.name());
    }
    
    public String getCurrencyCode() {
        return getMapStringValue(DataAttribute.CURRENCY_CODE.name());
    }

    private String getMapStringValue(String key) {
        Object valObj = data.get(key);
        final String val;
        if (valObj != null) {
            val = (String) valObj;
        } else {
            val = null;
        }
        return val;
    }
    
    private Integer getMapIntegerValue(String key) {
        Object valObj = data.get(key);
        final Integer val;
        if (valObj != null) {
            val = (Integer) valObj;
        } else {
            val = null;
        }
        return val;
    }

    private TimePoint getMapTimePointValue(String key) {
        Object valObj = data.get(key);
        final TimePoint val;
        if (valObj != null) {
            val = Subscription.getTime((long) valObj);
        } else {
            val = Subscription.getTime(0);
        }
        return val;
    }
}
