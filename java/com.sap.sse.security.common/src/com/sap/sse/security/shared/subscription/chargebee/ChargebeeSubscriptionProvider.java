package com.sap.sse.security.shared.subscription.chargebee;

import com.sap.sse.security.shared.subscription.SubscriptionDataHandler;
import com.sap.sse.security.shared.subscription.SubscriptionProvider;

public class ChargebeeSubscriptionProvider implements SubscriptionProvider {
    private static final String PROVIDER_NAME = "chargebee";

    private static ChargebeeSubscriptionProvider instance;
    private static ChargebeeSubscriptionDataHandler dataHanlder;

    public static ChargebeeSubscriptionProvider getInstance() {
        if (instance == null) {
            instance = new ChargebeeSubscriptionProvider();
        }
        return instance;
    }

    private ChargebeeSubscriptionProvider() {
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public SubscriptionDataHandler getDataHandler() {
        if (dataHanlder == null) {
            dataHanlder = new ChargebeeSubscriptionDataHandler();
        }
        return dataHanlder;
    }
}
