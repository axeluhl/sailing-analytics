package com.sap.sailing.server.gateway.subscription;

import org.json.simple.JSONObject;

public class SubscriptionWebhookEvent {
    public static final String EVENT_CUSTOMER_DELETED = "customer_deleted";
    public static final String EVENT_SUBSCRIPTION_DELETED = "subscription_deleted";
    public static final String EVENT_SUBSCRIPTION_CREATED = "subscription_created";
    public static final String EVENT_SUBSCRIPTION_CHANGED = "subscription_changed";
    public static final String EVENT_SUBSCRIPTION_CANCELLED = "subscription_cancelled";
    public static final String EVENT_PAYMENT_SUCCEEDED = "payment_succeeded";
    public static final String EVENT_PAYMENT_FAILED = "payment_failed";
    public static final String EVENT_SUBSCRIPTION_ACTIVATED = "subscription_activated";
    
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "active";
    public static final String INVOICE_STATUS_PAID = "paid";
    public static final String TRANSACTION_STATUS_SUCCESS = "success";
    public static final String TRANSACTION_TYPE_PAYMENT = "payment";
    
    private JSONObject eventJSON;
    private String eventId;
    private String eventType;
    private JSONObject content;
    
    public SubscriptionWebhookEvent(JSONObject eventJSON) {
        this.eventJSON = eventJSON;
        
        eventId = getJSONString(eventJSON, "id");
        eventType = toLowerCase(getJSONString(eventJSON, "event_type"));
        content = getJSONObject(eventJSON, "content");
    }
    
    public boolean isValidEvent() {
        return eventId != null && eventType != null;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }
    
    public String getCustomerEmail() {
        return getNestedJSONString(content, "customer", "email");
    }
    
    public String getCustomerId() {
        return getNestedJSONString(content, "customer", "id");
    }
    
    public String getPlanId() {
        return getNestedJSONString(content, "subscription", "plan_id");
    }
    
    public String getSubscriptionId() {
        return getNestedJSONString(content, "subscription", "id");
    }
    
    public String getSubscriptionStatus() {
        return toLowerCase(getNestedJSONString(content, "subscription", "status"));
    }
    
    public long getSubscriptionTrialStart() {
        return getNestedJSONLong(content, "subscription", "trial_start");
    }
    
    public long getSubscriptionTrialEnd() {
        return getNestedJSONLong(content, "subscription", "trial_end");
    }
    
    public long getSubscriptionCreatedAt() {
        return getNestedJSONLong(content, "subscription", "created_at");
    }
    
    public long getSubscriptionUpdatedAt() {
        return getNestedJSONLong(content, "subscription", "updated_at");
    }
    
    public long getEventOccurredAt() {
        return getJSONLong(eventJSON, "occurred_at");
    }
    
    public String getTransactionStatus() {
        return toLowerCase(getNestedJSONString(content, "transaction", "status"));
    }
    
    public String getInvoiceStatus() {
        return toLowerCase(getNestedJSONString(content, "invoice", "status"));
    }
    
    public String getTransactionType() {
        return toLowerCase(getNestedJSONString(content, "transaction", "type"));
    }
    
    private JSONObject getJSONObject(JSONObject object, String key) {
        return (JSONObject)object.get(key);
    }
    
    private String getJSONString(JSONObject object, String key) {
        return (String)object.get(key);
    }
    
    private long getJSONLong(JSONObject object, String key) {
        return (Long)object.get(key);
    }
    
    private Object getNestedJSONValue(JSONObject object, String ...keys) {
        JSONObject tmp = object;
        Object val = null;
        for (int i = 0; i < keys.length; i++) {
            if (i < keys.length - 1) {
                tmp = getJSONObject(tmp, keys[i]);
                if (tmp == null) {
                    break;
                }
            } else {
                val = tmp.get(keys[i]);
            }
        }
        
        return val;
    }
    
    private String getNestedJSONString(JSONObject object, String ...keys) {
        return (String)getNestedJSONValue(object, keys);
    }
    
    private long getNestedJSONLong(JSONObject object, String ...keys) {
        return (Long)getNestedJSONValue(object, keys);
    }
    
    private String toLowerCase(String str) {
        return str != null ? str.toLowerCase() : null;
    }
}
