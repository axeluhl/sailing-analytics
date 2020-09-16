package com.sap.sailing.server.gateway.subscription.chargebee;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.Subscription;

/**
 * Wrapped class for WebHook event JSON object
 * 
 * @author Tu Tran
 */
public class SubscriptionWebHookEvent {
    private static final Logger logger = Logger.getLogger(SubscriptionWebHookEvent.class.getName());
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static final String INVOICE_STATUS_PAID = "paid";

    private final JSONObject eventJSON;
    private final String eventId;
    private final SubscriptionWebHookEventType eventType;
    private final JSONObject content;

    public SubscriptionWebHookEvent(JSONObject eventJSON) {
        this.eventJSON = eventJSON;
        eventId = getJsonValue(eventJSON, "id");
        eventType = getEventType(eventJSON);
        content = getJsonValue(eventJSON, "content");
    }

    public boolean isValidEvent() {
        return eventId != null;
    }

    public String getEventId() {
        return eventId;
    }

    private SubscriptionWebHookEventType getEventType(JSONObject eventJSON) {
        final String eventTypeAsUppercaseString = toUpperCase(getJsonValue(eventJSON, "event_type"));
        try {
            return SubscriptionWebHookEventType.valueOf(eventTypeAsUppercaseString);
        } catch (NullPointerException | IllegalArgumentException e) {
            logger.warning("Exception trying to extract subscription event type " + eventTypeAsUppercaseString + ": "
                    + e.getMessage());
            return null;
        }
    }

    public SubscriptionWebHookEventType getEventType() {
        return eventType;
    }

    public String getCustomerEmail() {
        return getJsonValue(content, "customer", "email");
    }

    public String getCustomerId() {
        String customerId = getJsonValue(content, "customer", "id");
        if (StringUtils.isEmpty(customerId)) {
            customerId = getInvoiceCustomerId();
        }
        return customerId;
    }

    public String getPlanId() {
        return getJsonValue(content, "subscription", "plan_id");
    }

    public String getSubscriptionId() {
        String subscriptionId = getJsonValue(content, "subscription", "id");
        if (StringUtils.isEmpty(subscriptionId)) {
            subscriptionId = getInvoiceSubscriptionId();
        }
        return subscriptionId;
    }

    public String getSubscriptionStatus() {
        return toLowerCase(getJsonValue(content, "subscription", "status"));
    }

    public TimePoint getSubscriptionTrialStart() {
        return getTime(getJsonValue(content, "subscription", "trial_start"));
    }

    public TimePoint getSubscriptionTrialEnd() {
        return getTime(getJsonValue(content, "subscription", "trial_end"));
    }

    public TimePoint getSubscriptionCreatedAt() {
        return getTime(getJsonValue(content, "subscription", "created_at"));
    }

    public TimePoint getSubscriptionUpdatedAt() {
        return getTime(getJsonValue(content, "subscription", "updated_at"));
    }

    public TimePoint getEventOccurredAt() {
        return getTime(getJsonValue(eventJSON, "occurred_at"));
    }

    public String getTransactionStatus() {
        return toLowerCase(getJsonValue(content, "transaction", "status"));
    }

    public String getTransactionType() {
        return toLowerCase(getJsonValue(content, "transaction", "type"));
    }

    public String getInvoiceId() {
        return getJsonValue(content, "invoice", "id");
    }

    public String getInvoiceCustomerId() {
        return getJsonValue(content, "invoice", "customer_id");
    }

    public String getInvoiceSubscriptionId() {
        return getJsonValue(content, "invoice", "subscription_id");
    }

    public String getInvoiceStatus() {
        return toLowerCase(getJsonValue(content, "invoice", "status"));
    }

    private TimePoint getTime(long timestamp) {
        return Subscription.getTime(timestamp * 1000);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getJsonValue(JSONObject object, String... keys) {
        if (keys.length == 1) {
            return (T) object.get(keys[0]);
        } else {
            return (T) getNestedJsonValue(object, keys);
        }
    }

    private static <T> T getNestedJsonValue(JSONObject object, String... keys) {
        JSONObject tmp = object;
        T val = null;
        for (int i = 0; i < keys.length; i++) {
            if (i < keys.length - 1) {
                tmp = getJsonValue(tmp, keys[i]);
                if (tmp == null) {
                    break;
                }
            } else {
                val = getJsonValue(tmp, keys[i]);
            }
        }
        return val;
    }

    private String toLowerCase(String str) {
        return str != null ? str.toLowerCase() : null;
    }

    private String toUpperCase(String str) {
        return str != null ? str.toUpperCase() : null;
    }
}
