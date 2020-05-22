package com.sap.sailing.gwt.home.shared.places.user.profile.subscription;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sailing.gwt.ui.client.subscription.Chargebee;
import com.sap.sailing.gwt.ui.client.subscription.CheckoutOption;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionConfiguration;
import com.sap.sailing.gwt.ui.client.subscription.WithSubscriptionService;
import com.sap.sailing.gwt.ui.shared.subscription.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.authentication.WithAuthenticationManager;
import com.sap.sse.security.ui.authentication.WithUserService;

public class UserSubscriptionPresenter<C extends ClientFactoryWithDispatch & ErrorAndBusyClientFactory & WithAuthenticationManager & WithUserService & WithSubscriptionService>
        implements UserSubscriptionView.Presenter {

    private final C clientFactory;
    private UserSubscriptionView view;

    /**
     * Checkout success callback
     */
    private CheckoutOption.SuccessCallback onCheckoutSuccessCallback = new CheckoutOption.SuccessCallback() {

        @Override
        public void call(String hostedPageId) {
            requestFinishingPlanUpdating(hostedPageId);
        }
    };

    /**
     * Checkout fail callback
     */
    private CheckoutOption.ErrorCallback onCheckoutErrorCallback = new CheckoutOption.ErrorCallback() {

        @Override
        public void call(String error) {
            view.onOpenCheckoutError(error);
        }
    };

    /**
     * Checkout modal close callback
     */
    private CheckoutOption.CloseCallback onCheckoutCloseCallback = new CheckoutOption.CloseCallback() {

        @Override
        public void call() {
            view.onCloseCheckoutModal();
        }
    };

    public UserSubscriptionPresenter(C clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void init() {
        Chargebee.init(Chargebee.InitOption.create(SubscriptionConfiguration.CHARGEBEE_SITE));
    }

    @Override
    public void loadSubscription() {
        view.onStartLoadSubscription();

        clientFactory.getSubscriptionService().getSubscription(new AsyncCallback<SubscriptionDTO>() {

            @Override
            public void onSuccess(SubscriptionDTO result) {
                if (result != null && result.getError() != null && !result.getError().isEmpty()) {
                    showError(StringMessages.INSTANCE.errorLoadingUserSubscription(result.getError()));
                    return;
                }

                view.updateView(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                showError(StringMessages.INSTANCE.errorLoadingUserSubscription(caught.getMessage()));
            }
        });
    }

    @Override
    public void setView(UserSubscriptionView view) {
        this.view = view;
    }

    @Override
    public void openCheckout(String planId) {
        clientFactory.getSubscriptionService().generateHostedPageObject(planId,
                new AsyncCallback<HostedPageResultDTO>() {

                    @Override
                    public void onSuccess(HostedPageResultDTO hostedPage) {
                        if (hostedPage.getError() != null && !hostedPage.getError().isEmpty()) {
                            view.onOpenCheckoutError(hostedPage.getError());
                        } else if (hostedPage.getHostedPageJSONString() != null
                                && !hostedPage.getHostedPageJSONString().isEmpty()) {
                            Chargebee.getInstance()
                                    .openCheckout(CheckoutOption.create(hostedPage.getHostedPageJSONString(),
                                            onCheckoutSuccessCallback, onCheckoutErrorCallback,
                                            onCheckoutCloseCallback));
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

    @Override
    public void cancelSubscription() {
        clientFactory.getSubscriptionService().cancelSubscription(new AsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    showError(StringMessages.INSTANCE.failedCancelSubscription());
                    return;
                }

                view.updateView(null);
            }

            @Override
            public void onFailure(Throwable caught) {
                showError(StringMessages.INSTANCE.errorCancelSubscription(caught.getMessage()));
            }
        });
    }

    private void requestFinishingPlanUpdating(String hostedPageId) {
        clientFactory.getSubscriptionService().updatePlanSuccess(hostedPageId, new AsyncCallback<SubscriptionDTO>() {

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
