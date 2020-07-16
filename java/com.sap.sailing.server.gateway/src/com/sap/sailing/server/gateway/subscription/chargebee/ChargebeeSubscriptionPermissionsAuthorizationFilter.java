package com.sap.sailing.server.gateway.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.subscription.SubscriptionPermissionsAuthorizationFilter;

public class ChargebeeSubscriptionPermissionsAuthorizationFilter extends SubscriptionPermissionsAuthorizationFilter {
    private static final Logger logger = Logger
            .getLogger(ChargebeeSubscriptionPermissionsAuthorizationFilter.class.getName());

    @Override
    protected String getSubscriptionUserName(ServletRequest request) {
        try {
            final Object requestBody = JSONValue.parseWithException(request.getReader());
            final JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
            final SubscriptionWebHookEvent event = new SubscriptionWebHookEvent(requestObject);
            if (!event.isValidEvent()) {
                throw new IllegalArgumentException("Invalid webhook event");
            }
            return event.getCustomerId();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Subscription permission checking error", e);
        }
        return null;
    }
}
