package com.sap.sailing.gwt.ui.client.subscription.chargebee;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;
import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionClientProvider;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionViewPresenter;
import com.sap.sse.gwt.client.EntryPointHelper;

public class ChargebeeSubscriptionClientProvider implements SubscriptionClientProvider {
    public static final String PROVIDER_NAME = "chargebee";

    private SubscriptionViewPresenter viewPresenter;
    private ChargebeeSubscriptionServiceAsync service;
    private ChargebeeSubscriptionWriteServiceAsync writeService;

    @Override
    public void init() {
        Chargebee.init(Chargebee.InitOption.create(SubscriptionConfiguration.CHARGEBEE_SITE));
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public ChargebeeSubscriptionWriteServiceAsync getSubscriptionWriteService() {
        if (writeService == null) {
            writeService = GWT.create(ChargebeeSubscriptionWriteService.class);
        }
        return writeService;
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
        String servicePath = serviceBasePath + "/" + getProviderName();
        EntryPointHelper.registerASyncService((ServiceDefTarget) getSubscriptionService(), servicePath,
                HEADER_FORWARD_TO_REPLICA);
        EntryPointHelper.registerASyncService((ServiceDefTarget) getSubscriptionWriteService(), servicePath,
                HEADER_FORWARD_TO_MASTER);
    }

    @Override
    public SubscriptionViewPresenter getSubscriptionViewPresenter() {
        if (viewPresenter == null) {
            viewPresenter = new ChargebeeSubscriptionViewPresenter(getSubscriptionService(),
                    getSubscriptionWriteService());
        }
        return viewPresenter;
    }

}
