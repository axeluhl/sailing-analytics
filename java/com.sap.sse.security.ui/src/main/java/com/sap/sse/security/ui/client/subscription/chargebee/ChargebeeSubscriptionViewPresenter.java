package com.sap.sse.security.ui.client.subscription.chargebee;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.subscription.BaseUserSubscriptionView;
import com.sap.sse.security.ui.client.subscription.SubscriptionViewPresenter;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.FinishCheckoutDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.PrepareCheckoutDTO;

public class ChargebeeSubscriptionViewPresenter implements SubscriptionViewPresenter {
    private ChargebeeSubscriptionServiceAsync service;
    private ChargebeeSubscriptionWriteServiceAsync writeService;

    public ChargebeeSubscriptionViewPresenter(ChargebeeSubscriptionServiceAsync service,
            ChargebeeSubscriptionWriteServiceAsync writeService) {
        this.service = service;
        this.writeService = writeService;
    }

    @Override
    public void startCheckout(String planId, BaseUserSubscriptionView view) {
        service.prepareCheckout(planId, new AsyncCallback<PrepareCheckoutDTO>() {
            @Override
            public void onSuccess(PrepareCheckoutDTO hostedPage) {
                if (hostedPage.getError() != null && !hostedPage.getError().isEmpty()) {
                    view.onOpenCheckoutError(hostedPage.getError());
                } else if (hostedPage.getHostedPageJSONString() != null
                        && !hostedPage.getHostedPageJSONString().isEmpty()) {
                    Chargebee.getInstance().openCheckout(CheckoutOption.create(hostedPage.getHostedPageJSONString(),
                            new CheckoutOption.SuccessCallback() {
                                @Override
                                public void call(String hostedPageId) {
                                    requestFinishingPlanUpdating(hostedPageId, view);
                                }
                            }, new CheckoutOption.ErrorCallback() {
                                @Override
                                public void call(String error) {
                                    view.onOpenCheckoutError(error);
                                }
                            }, new CheckoutOption.CloseCallback() {
                                @Override
                                public void call() {
                                    view.onCloseCheckoutModal();
                                }
                            }));
                } else {
                    view.onOpenCheckoutError(StringMessages.INSTANCE.failGeneratingHostedPageObject());
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                view.onOpenCheckoutError(StringMessages.INSTANCE.errorOpenCheckout(caught.getMessage()));
            }
        });
    }

    private void requestFinishingPlanUpdating(String hostedPageId, BaseUserSubscriptionView view) {
        final FinishCheckoutDTO data = new FinishCheckoutDTO();
        data.setHostedPageId(hostedPageId);
        writeService.finishCheckout(/* planId */ null, data, new AsyncCallback<SubscriptionDTO>() {
            @Override
            public void onSuccess(SubscriptionDTO result) {
                updateView(result, view);
                Chargebee.getInstance().closeAll();
            }

            @Override
            public void onFailure(Throwable caught) {
                Chargebee.getInstance().closeAll();
                showError(StringMessages.INSTANCE.errorSaveSubscription(caught.getMessage()));
            }
        });
    }
    
    private void updateView(SubscriptionDTO subscription, BaseUserSubscriptionView view) {
        service.getAllSubscriptionPlans(new AsyncCallback<Iterable<SubscriptionPlanDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                // This will simply not refresh the SubscriptionPlan list in the view.
                // Not critical, since the case of a changed set of SubscriptionPlans is highly unlikely.
                view.updateView(subscription, null);
            }
            @Override
            public void onSuccess(Iterable<SubscriptionPlanDTO> result) {
                view.updateView(subscription, result);
            }
        });
    }

    private void showError(String message) {
        Notification.notify(message, NotificationType.ERROR);
    }
}
