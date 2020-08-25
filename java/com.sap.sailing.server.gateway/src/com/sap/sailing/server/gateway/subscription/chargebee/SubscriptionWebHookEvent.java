package com.sap.sailing.server.gateway.subscription.chargebee;

import java.util.logging.Logger;

import org.json.simple.JSONObject;

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
        return getJsonValue(content, "customer", "id");
    }

    public String getPlanId() {
        return getJsonValue(content, "subscription", "plan_id");
    }

    public String getSubscriptionId() {
        return getJsonValue(content, "subscription", "id");
    }

    public String getSubscriptionStatus() {
        return toLowerCase(getJsonValue(content, "subscription", "status"));
    }

    public long getSubscriptionTrialStart() {
        return getJsonValue(content, "subscription", "trial_start");
    }

    public long getSubscriptionTrialEnd() {
        return getJsonValue(content, "subscription", "trial_end");
    }

    public long getSubscriptionCreatedAt() {
        return getJsonValue(content, "subscription", "created_at");
    }

    public long getSubscriptionUpdatedAt() {
        return getJsonValue(content, "subscription", "updated_at");
    }

    public long getEventOccurredAt() {
        return getJsonValue(eventJSON, "occurred_at");
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
