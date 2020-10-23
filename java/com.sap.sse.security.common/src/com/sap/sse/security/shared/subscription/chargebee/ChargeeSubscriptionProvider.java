package com.sap.sse.security.shared.subscription.chargebee;

import com.sap.sse.security.shared.subscription.SubscriptionDataHandler;
import com.sap.sse.security.shared.subscription.SubscriptionProvider;

public class ChargeeSubscriptionProvider implements SubscriptionProvider {
    private static final String PROVIDER_NAME = "chargebee";

    private static ChargeeSubscriptionProvider instance;
    private static ChargebeeSubscriptionDataHandler dataHanlder;

    public static ChargeeSubscriptionProvider getInstance() {
        if (instance == null) {
            instance = new ChargeeSubscriptionProvider();
        }
        return instance;
    }

    private ChargeeSubscriptionProvider() {
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public SubscriptionDataHandler getDataHandler() {
        if (dataHanlder==null) {
            dataHanlder=new ChargebeeSubscriptionDataHandler();
        }
        return dataHanlder;
    }
}
