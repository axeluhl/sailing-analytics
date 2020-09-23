package com.sap.sailing.gwt.ui.client.subscription;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.ChargebeeSubscriptionService;
import com.sap.sailing.gwt.ui.client.subscription.chargebee.ChargebeeSubscriptionServiceAsync;

/**
 * SubscriptionServiceFactory registers all payment provider services in the system and provides way to access a
 * specific service
 */
public class SubscriptionServiceFactory {
    private static final String CHARGEBEE_ID = "chargebee";

    private static SubscriptionServiceFactory instance;

    private Map<String, SubscriptionServiceAsync<?, ?>> services;

    public static SubscriptionServiceFactory getInstance() {
        if (instance == null) {
            instance = new SubscriptionServiceFactory();
        }
        return instance;
    }

    private SubscriptionServiceFactory() {
        services = new HashMap<String, SubscriptionServiceAsync<?, ?>>();
        registerServices();
    }

    public ChargebeeSubscriptionServiceAsync getChargebeeService() {
        return (ChargebeeSubscriptionServiceAsync) services.get(CHARGEBEE_ID);
    }

    /**
     * Return default payment service for handling new user subscriptions
     */
    public SubscriptionServiceAsync<?, ?> getDefaultService() {
        return getChargebeeService();
    }

    private void registerServices() {
        registerSubscriptionService(CHARGEBEE_ID, GWT.create(ChargebeeSubscriptionService.class));
    }

    private <P, F> void registerSubscriptionService(String id, SubscriptionServiceAsync<P, F> service) {
        services.put(id, service);
    }
}
