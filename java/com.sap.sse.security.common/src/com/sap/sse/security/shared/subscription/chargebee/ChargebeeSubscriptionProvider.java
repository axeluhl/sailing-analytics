package com.sap.sse.security.shared.subscription.chargebee;

import com.chargebee.Environment;
import com.sap.sse.security.shared.subscription.SubscriptionApiService;
import com.sap.sse.security.shared.subscription.SubscriptionDataHandler;
import com.sap.sse.security.shared.subscription.SubscriptionProvider;

public class ChargebeeSubscriptionProvider implements SubscriptionProvider {
    private static final String PROVIDER_NAME = "chargebee";

    private static ChargebeeSubscriptionProvider instance;
    private static ChargebeeSubscriptionDataHandler dataHanlder;
    private static ChargebeeApiService apiService;

    private static boolean inited;

    public static ChargebeeSubscriptionProvider getInstance() {
        if (instance == null) {
            initialize();
            instance = new ChargebeeSubscriptionProvider();
        }
        return instance;
    }

    public static void initialize() {
        if (!inited) {
            Environment.configure(ChargebeeConfiguration.getInstance().getSite(),
                    ChargebeeConfiguration.getInstance().getApiKey());
        }
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

    @Override
    public SubscriptionApiService getApiService() {
        if (apiService == null) {
            apiService = new ChargebeeApiService();
        }
        return apiService;
    }
}
