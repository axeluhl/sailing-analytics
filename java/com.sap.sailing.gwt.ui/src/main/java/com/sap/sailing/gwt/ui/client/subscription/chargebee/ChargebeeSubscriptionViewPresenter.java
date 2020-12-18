package com.sap.sailing.gwt.ui.client.subscription.chargebee;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.places.user.profile.subscription.UserSubscriptionView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionViewPresenter;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.FinishCheckoutDTO;
import com.sap.sailing.gwt.ui.shared.subscription.chargebee.PrepareCheckoutDTO;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public class ChargebeeSubscriptionViewPresenter implements SubscriptionViewPresenter {
    private ChargebeeSubscriptionServiceAsync service;
    private ChargebeeSubscriptionWriteServiceAsync writeService;

    public ChargebeeSubscriptionViewPresenter(ChargebeeSubscriptionServiceAsync service,
            ChargebeeSubscriptionWriteServiceAsync writeService) {
        this.service = service;
        this.writeService = writeService;
    }

    @Override
    public void startCheckout(String planId, UserSubscriptionView view) {
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
                    ;
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

    private void requestFinishingPlanUpdating(String hostedPageId, UserSubscriptionView view) {
        final FinishCheckoutDTO data = new FinishCheckoutDTO();
        data.setHostedPageId(hostedPageId);
        writeService.finishCheckout(null, data, new AsyncCallback<SubscriptionDTO>() {

            @Override
            public void onSuccess(SubscriptionDTO result) {
                view.updateView(result);
                Chargebee.getInstance().closeAll();
            }

            @Override
            public void onFailure(Throwable caught) {
                Chargebee.getInstance().closeAll();
                showError(StringMessages.INSTANCE.errorSaveSubscription(caught.getMessage()));
            }
        });
    }

    private void showError(String message) {
        Notification.notify(message, NotificationType.ERROR);
    }
}
