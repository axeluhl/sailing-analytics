package com.sap.sailing.server.gateway.subscription;

import org.json.simple.JSONObject;

/**
 * Wrapped class for WebHook event JSON object
 * 
 * @author tutran
 */
public class SubscriptionWebHookEvent {

    public static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static final String INVOICE_STATUS_PAID = "paid";
    public static final String TRANSACTION_STATUS_SUCCESS = "success";
    public static final String TRANSACTION_TYPE_PAYMENT = "payment";

    private JSONObject eventJSON;
    private String eventId;
    private String eventType;
    private JSONObject content;

    public SubscriptionWebHookEvent(JSONObject eventJSON) {
        this.eventJSON = eventJSON;

        eventId = getJsonValue(eventJSON, "id");
        eventType = toLowerCase(getJsonValue(eventJSON, "event_type"));
        content = getJsonValue(eventJSON, "content");
    }

    public boolean isValidEvent() {
        return eventId != null && eventType != null;
    }

    public String getEventId() {
        return eventId;
    }

    public SubscriptionWebHookEventType getEventType() {
        try {
            return SubscriptionWebHookEventType.valueOf(eventType.toUpperCase());
        } catch (Exception e) {
            return null;
        }
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

    public String getInvoiceStatus() {
        return toLowerCase(getJsonValue(content, "invoice", "status"));
    }

    public String getTransactionType() {
        return toLowerCase(getJsonValue(content, "transaction", "type"));
    }

    @SuppressWarnings("unchecked")
    private <T> T getJsonValue(JSONObject object, String... keys) {
        if (keys.length == 1) {
            return (T) object.get(keys[0]);
        } else {
            return (T) getNestedJsonValue(object, keys);
        }
    }

    private <T> T getNestedJsonValue(JSONObject object, String... keys) {
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
}
