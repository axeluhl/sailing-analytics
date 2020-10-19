package com.sap.sailing.gwt.ui.client.subscription.chargebee;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionClientProvider;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionViewPresenter;
import com.sap.sse.gwt.client.EntryPointHelper;

public class ChargebeeSubscriptionClientProvider implements SubscriptionClientProvider {
    public static final String PROVIDER_NAME = "chargebee";

    private SubscriptionViewPresenter viewPresenter;
    private ChargebeeSubscriptionServiceAsync service;

    @Override
    public void init() {
        Chargebee.init(Chargebee.InitOption.create(SubscriptionConfiguration.CHARGEBEE_SITE));
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public ChargebeeSubscriptionServiceAsync getSubscriptionService() {
        if (service == null) {
            service = GWT.create(ChargebeeSubscriptionService.class);
        }
        return service;
    }

    @Override
    public void registerAsyncService(String serviceBasePath) {
        EntryPointHelper.registerASyncService((ServiceDefTarget) getSubscriptionService(),
                serviceBasePath + "/" + getProviderName());
    }

    @Override
    public SubscriptionViewPresenter getSubscriptionViewPresenter() {
        if (viewPresenter == null) {
            viewPresenter = new ChargebeeSubscriptionViewPresenter(getSubscriptionService());
        }
        return viewPresenter;
    }
}
