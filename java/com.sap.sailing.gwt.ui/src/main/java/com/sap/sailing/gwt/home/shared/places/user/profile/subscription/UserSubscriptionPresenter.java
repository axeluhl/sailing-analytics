package com.sap.sailing.gwt.home.shared.places.user.profile.subscription;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.shared.subscription.InvalidSubscriptionProviderException;
import com.sap.sse.security.ui.authentication.WithAuthenticationManager;
import com.sap.sse.security.ui.authentication.WithUserService;
import com.sap.sse.security.ui.client.subscription.SubscriptionServiceAsync;
import com.sap.sse.security.ui.client.subscription.WithSubscriptionService;
import com.sap.sse.security.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

/**
 * Implementation presenter of {@link UserSubscriptionView.Presenter}
 * 
 * @author Tu Tran
 */
public class UserSubscriptionPresenter<C extends ClientFactoryWithDispatch & ErrorAndBusyClientFactory & WithAuthenticationManager & WithUserService & WithSubscriptionService>
        implements UserSubscriptionView.Presenter {

    private final C clientFactory;
    private UserSubscriptionView view;
    private Map<Serializable, SubscriptionPlanDTO> subscriptionPlans = new HashMap<>();

    public UserSubscriptionPresenter(C clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void init() {
        clientFactory.getSubscriptionService().initializeProviders();
    }
    
    @Override
    public void loadSubscription() {
        view.onStartLoadSubscription();
        fetchSubscription();
    }

    @Override
    public void setView(UserSubscriptionView view) {
        this.view = view;
    }

    @Override
    public void openCheckout(String planId) {
        try {
            clientFactory.getSubscriptionService().getDefaultProvider().getSubscriptionViewPresenter()
                    .startCheckout(planId, view);
        } catch (InvalidSubscriptionProviderException e) {
            onInvalidSubscriptionProviderError(e);
        }
    }

    @Override
    public void cancelSubscription(String planId, String providerName) {
        try {
            clientFactory.getSubscriptionService().getWriteAsyncServiceByProvider(providerName)
                    .cancelSubscription(planId, new AsyncCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            if (!result) {
                                showError(StringMessages.INSTANCE.failedCancelSubscription());
                            } else {
                                fetchSubscription();
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            showError(StringMessages.INSTANCE.errorCancelSubscription(caught.getMessage()));
                        }
                    });
        } catch (InvalidSubscriptionProviderException e) {
            onInvalidSubscriptionProviderError(e);
        }
    }

    private void fetchSubscription() {
        try {
            clientFactory.getSubscriptionService().getDefaultAsyncService()
                    .getSubscription(new AsyncCallback<SubscriptionDTO>() {
                        @Override
                        public void onSuccess(SubscriptionDTO result) {
                            if (result != null && result.getError() != null && !result.getError().isEmpty()) {
                                showError(StringMessages.INSTANCE.errorLoadingUserSubscription(result.getError()));
                            } else {
                                updateView(result);
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            showError(StringMessages.INSTANCE.errorLoadingUserSubscription(caught.getMessage()));
                        }
                    });
        } catch (InvalidSubscriptionProviderException e) {
            onInvalidSubscriptionProviderError(e);
        }
    }
    
    private void updateView(SubscriptionDTO subscription) {
        try {
            final SubscriptionServiceAsync<?, ?> defaultAsyncService = clientFactory.getSubscriptionService()
                    .getDefaultAsyncService();
            
            defaultAsyncService.getAllSubscriptionPlansMappedById(new AsyncCallback<Map<Serializable, SubscriptionPlanDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    // This will simply not refresh the SubscriptionPlan list in the view.
                    // Not critical, since the case of a changed set of SubscriptionPlans is highly unlikely.
                    view.updateView(subscription, null);
                }

                @Override
                public void onSuccess(Map<Serializable, SubscriptionPlanDTO> result) {
                    updateSubscriptionPlanMap(result);
                    view.updateView(subscription, result.values());
                }
            });
        } catch (InvalidSubscriptionProviderException e) {
            onInvalidSubscriptionProviderError(e);
        }
    }
    
    private void updateSubscriptionPlanMap(Map<Serializable, SubscriptionPlanDTO> updatedPlans) {
        subscriptionPlans.clear();
        subscriptionPlans.putAll(updatedPlans);
    }
    
    private void showError(String message) {
        Notification.notify(message, NotificationType.ERROR);
    }

    private void onInvalidSubscriptionProviderError(InvalidSubscriptionProviderException e) {
        showError(StringMessages.INSTANCE.errorInvalidSubscritionProvider(e.getMessage()));
    }

    @Override
    public SubscriptionPlanDTO getPlanById(String planId) {
        return subscriptionPlans.get(planId);
    }
}
