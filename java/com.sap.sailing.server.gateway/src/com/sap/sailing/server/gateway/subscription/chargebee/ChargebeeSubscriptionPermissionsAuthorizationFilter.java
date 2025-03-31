package com.sap.sailing.server.gateway.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.subscription.SubscriptionPermissionsAuthorizationFilter;

/**
 * Shiro authorization filter for Chargebee webhook handler servlet, implementation of
 * {@code SubscriptionPermissionsAuthorizationFilter}. Use this filter by adding configuration into shiro.ini:<br>
 * <code>
 * subscriptionPermissions = com.sap.sailing.server.gateway.subscription.chargebee.ChargebeeSubscriptionPermissionsAuthorizationFilter
 * /subscription/hooks = bearerToken,subscriptionPermissions
 * </code>
 * 
 * @author Tu Tran
 */
public class ChargebeeSubscriptionPermissionsAuthorizationFilter extends SubscriptionPermissionsAuthorizationFilter {
    private static final Logger logger = Logger
            .getLogger(ChargebeeSubscriptionPermissionsAuthorizationFilter.class.getName());

    @Override
    protected String getSubscriptionUserName(ServletRequest request) {
        try {
            final Object requestBody = JSONValue.parseWithException(request.getReader());
            final JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
            logger.info(() -> "Received Chargebee webhook: " + requestObject.toJSONString());
            final SubscriptionWebHookEvent event = new SubscriptionWebHookEvent(requestObject);
            if (!event.isValidEvent()) {
                throw new IllegalArgumentException("Invalid Chargebee webhook event");
            }
            request.setAttribute("event", event);
            return event.getCustomerId();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Chargebee subscription webhook permission checking error", e);
            return null;
        }
    }
}
