package com.sap.sse.security.ui.client.subscription.chargebee;

import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_MASTER;
import static com.sap.sse.common.HttpRequestHeaderConstants.HEADER_FORWARD_TO_REPLICA;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.sap.sse.gwt.client.EntryPointHelper;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.subscription.SubscriptionClientProvider;
import com.sap.sse.security.ui.client.subscription.SubscriptionViewPresenter;
import com.sap.sse.security.ui.shared.subscription.chargebee.ChargebeeConfigurationDTO;

public class ChargebeeSubscriptionClientProvider implements SubscriptionClientProvider {
    public static final String PROVIDER_NAME = "chargebee";

    private SubscriptionViewPresenter viewPresenter;
    private ChargebeeSubscriptionServiceAsync service;
    private ChargebeeSubscriptionWriteServiceAsync writeService;

    final private AsyncCallback<ChargebeeConfigurationDTO> configurationCallback = new AsyncCallback<ChargebeeConfigurationDTO>() {
        @Override
        public void onSuccess(ChargebeeConfigurationDTO result) {
            if (result != null) {
                Chargebee.init(Chargebee.InitOption.create(result.getSiteName()));
            }
        }

        @Override
        public void onFailure(Throwable caught) {
            Notification.notify(caught.getMessage(), NotificationType.ERROR);
        }
    };

    @Override
    public void init() {
        getSubscriptionWriteService().getConfiguration(configurationCallback);
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
