package com.sap.sse.security.ui.client.subscription.chargebee;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.subscription.SubscribeView;
import com.sap.sse.security.ui.client.subscription.SubscriptionViewPresenter;
import com.sap.sse.security.ui.shared.subscription.SubscriptionListDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.FinishCheckoutDTO;
import com.sap.sse.security.ui.shared.subscription.chargebee.PrepareCheckoutDTO;

public class ChargebeeSubscriptionViewPresenter implements SubscriptionViewPresenter {

    private final ChargebeeSubscriptionServiceAsync service;
    private final ChargebeeSubscriptionWriteServiceAsync writeService;

    public ChargebeeSubscriptionViewPresenter(final ChargebeeSubscriptionServiceAsync service,
            final ChargebeeSubscriptionWriteServiceAsync writeService) {
        this.service = service;
        this.writeService = writeService;
    }

    @Override
    public void startCheckout(final String planId, final SubscribeView view, final Runnable fireUserUpdateEvent) {
        service.prepareCheckout(planId, new AsyncCallback<PrepareCheckoutDTO>() {
            @Override
            public void onSuccess(final PrepareCheckoutDTO hostedPage) {
                if (hostedPage.getError() != null && !hostedPage.getError().isEmpty()) {
                    view.onOpenCheckoutError(hostedPage.getError());
                } else if (hostedPage.getHostedPageJSONString() != null
                        && !hostedPage.getHostedPageJSONString().isEmpty()) {
                    Chargebee.getInstance().openCheckout(CheckoutOption.create(hostedPage.getHostedPageJSONString(),
                            new CheckoutOption.SuccessCallback() {
                                @Override
                                public void call(final String hostedPageId) {
                                    requestFinishingPlanUpdating(hostedPageId, view, fireUserUpdateEvent);
                                }
                            }, new CheckoutOption.ErrorCallback() {
                                @Override
                                public void call(final String error) {
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
            public void onFailure(final Throwable caught) {
                view.onOpenCheckoutError(StringMessages.INSTANCE.errorOpenCheckout(caught.getMessage()));
            }
        });
    }

    private void requestFinishingPlanUpdating(final String hostedPageId, final SubscribeView view,
            final Runnable fireUserUpdateEvent) {
        final FinishCheckoutDTO data = new FinishCheckoutDTO();
        data.setHostedPageId(hostedPageId);
        writeService.finishCheckout(data, new AsyncCallback<SubscriptionListDTO>() {
            @Override
            public void onSuccess(final SubscriptionListDTO result) {
                fireUserUpdateEvent.run();
                Chargebee.getInstance().closeAll();
            }

            @Override
            public void onFailure(final Throwable caught) {
                Chargebee.getInstance().closeAll();
                view.onOpenCheckoutError(StringMessages.INSTANCE.errorSaveSubscription(caught.getMessage()));
            }
        });
    }

}
