package com.sap.sse.security.subscription;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.subscription.SubscriptionData.DataAttribute;

/**
 * AbstractSubscriptionDataHandler provides implementation of {@code SubscriptionDataHandler} for serializing commons
 * subscription attributes to data map. Sub-class might append some specific subscription data to the map, or totally
 * change the implementation
 */
abstract public class AbstractSubscriptionDataHandler implements SubscriptionDataHandler {
    @Override
    public Map<String, Object> toMap(Subscription subscription) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(DataAttribute.SUBSCRIPTION_ID.name(), subscription.getSubscriptionId());
        data.put(DataAttribute.CUSTOMER_ID.name(), subscription.getCustomerId());
        data.put(DataAttribute.PLAN_ID.name(), subscription.getPlanId());
        data.put(DataAttribute.TRIAL_START.name(), subscription.getTrialStart().asMillis());
        data.put(DataAttribute.TRIAL_END.name(), subscription.getTrialEnd().asMillis());
        data.put(DataAttribute.SUBSCRIPTION_STATUS.name(), subscription.getSubscriptionStatus());
        data.put(DataAttribute.PAYMENT_STATUS.name(), subscription.getPaymentStatus());
        data.put(DataAttribute.TRANSACTION_TYPE.name(), subscription.getTransactionType());
        data.put(DataAttribute.TRANSACTION_STATUS.name(), subscription.getTransactionStatus());
        data.put(DataAttribute.INVOICE_ID.name(), subscription.getInvoiceId());
        data.put(DataAttribute.INVOICE_STATUS.name(), subscription.getInvoiceStatus());
        data.put(DataAttribute.SUBSCRIPTION_CREATED_AT.name(), subscription.getSubscriptionCreatedAt().asMillis());
        data.put(DataAttribute.SUBSCRIPTION_UPDATED_AT.name(), subscription.getSubscriptionUpdatedAt().asMillis());
        data.put(DataAttribute.LATEST_EVENT_TIME.name(), subscription.getLatestEventTime().asMillis());
        data.put(DataAttribute.MANUAL_UPDATED_AT.name(), subscription.getManualUpdatedAt().asMillis());
        data.put(DataAttribute.PROVIDER_NAME.name(), subscription.getProviderName());
        data.put(DataAttribute.REOCURRING_PAYMENT_VALUE.name(), subscription.getReoccuringPaymentValue());
        data.put(DataAttribute.CURRENCY_CODE.name(), subscription.getCurrencyCode());
        data.put(DataAttribute.SUBSCRIPTION_ACTIVATED_AT.name(), subscription.getSubscriptionActivatedAt().asMillis());
        data.put(DataAttribute.NEXT_BILLING_AT.name(), subscription.getNextBillingAt().asMillis());
        data.put(DataAttribute.CURRENT_TERM_END.name(), subscription.getCurrentTermEnd().asMillis());
        data.put(DataAttribute.CANCELLED_AT.name(), subscription.getCancelledAt().asMillis());
        return data;
    }
}
