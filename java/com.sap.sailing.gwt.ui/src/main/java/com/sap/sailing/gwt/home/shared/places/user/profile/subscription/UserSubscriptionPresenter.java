package com.sap.sailing.gwt.home.shared.places.user.profile.subscription;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sailing.gwt.ui.client.subscription.Chargebee;
import com.sap.sailing.gwt.ui.client.subscription.CheckoutOption;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionConfiguration;
import com.sap.sailing.gwt.ui.client.subscription.WithSubscriptionService;
import com.sap.sailing.gwt.ui.shared.subscription.HostedPageResultDTO;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.authentication.WithAuthenticationManager;
import com.sap.sse.security.ui.authentication.WithUserService;

/**
 * Presenter for {@link UserSubscriptionView}, implementation of {@link UserSubscriptionView.Presenter}}, 
 * which handles initializing Chargebee, opening and executing checkout,
 * requesting user subscription data, and canceling user subscription
 * 
 * @author tutran
 */
public class UserSubscriptionPresenter<C extends ClientFactoryWithDispatch & ErrorAndBusyClientFactory & WithAuthenticationManager & WithUserService & WithSubscriptionService>
    implements UserSubscriptionView.Presenter {
    
    private final C clientFactory;
    private UserSubscriptionView view;
    
    /**
     * Callback for Chargebee checkout success event
     */
    private CheckoutOption.SuccessCallback onCheckoutSuccessCallback = 
            new CheckoutOption.SuccessCallback() {
        
                @Override
                public void call(String hostedPageId) {
                    requestFinishingPlanUpgrading(hostedPageId);
                }
            };
            
    /**
     * Callback for Chargebee checkout fail event
     */
    private CheckoutOption.ErrorCallback onCheckoutErrorCallback =
            new CheckoutOption.ErrorCallback() {
        
                @Override
                public void call(String error) {
                    view.onOpenCheckoutError(error);
                }
            };
            
    /**
     * Callback for Chargebee checkout modal close event
     */
    private CheckoutOption.CloseCallback onCheckoutCloseCallback =
            new CheckoutOption.CloseCallback() {
        
                @Override
                public void call() {
                    view.onCloseCheckoutModal();
                }
            };
    
    public UserSubscriptionPresenter(C clientFactory) {
        this.clientFactory = clientFactory;
    }
    
    /**
     * Init Chargebee
     */
    @Override
    public void init() {
        Chargebee.init(Chargebee.InitOption.create(SubscriptionConfiguration.CHARGEBEE_SITE));
    }

    /**
     * Load user's subscription data
     */
    @Override
    public void loadSubscription() {
        view.onStartLoadSubscription();
        
        clientFactory.getSubscriptionService().getSubscription(new AsyncCallback<SubscriptionDTO>() {
            
            @Override
            public void onSuccess(SubscriptionDTO result) {
                if (result != null && result.error != null && !result.error.isEmpty()) {
                    Window.alert("Get user subscription error: " + result.error);
                    return;
                }
                
                view.updateView(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Get user subscription failed: " + caught.getMessage());
            }
        });
    }

    @Override
    public void setView(UserSubscriptionView view) {
        this.view = view;        
    }

    /**
     * Open Chargebee checkout modal from which user can create new subscription or change one if user is already in a subscription plan
     * 
     * @param planId Id of plan user want to subscribe to
     */
    @Override
    public void openCheckout(String planId) {
        clientFactory.getSubscriptionService().generateHostedPageObject(planId, new AsyncCallback<HostedPageResultDTO>() {
            
            @Override
            public void onSuccess(HostedPageResultDTO hostedPage) {
                if (hostedPage.error != null && !hostedPage.error.isEmpty()) {
                    view.onOpenCheckoutError(hostedPage.error);
                } else if (hostedPage.hostedPageJSONString != null && !hostedPage.hostedPageJSONString.isEmpty()) {
                    Chargebee.getInstance().openCheckout(
                            CheckoutOption.create(
                                hostedPage.hostedPageJSONString,
                                onCheckoutSuccessCallback,
                                onCheckoutErrorCallback,
                                onCheckoutCloseCallback
                            ));;
                } else {
                    view.onOpenCheckoutError("Failed to generating hosted page object, please try again");
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                view.onOpenCheckoutError("Checkout error: " + caught.getMessage());
            }
        });
    }
    
    /**
     * Cancel current user's subscription
     */
    @Override
    public void cancelSubscription() {
        clientFactory.getSubscriptionService().cancelSubscription(new AsyncCallback<Boolean>() {
            
            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    Window.alert("Failed to cancel the subscription");
                    return;
                }
                
                view.updateView(null);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Cancel subscription error: " + caught.getMessage());
            }
        });
    }
    
    private void requestFinishingPlanUpgrading(String hostedPageId) {
        clientFactory.getSubscriptionService().updatePlanSuccess(hostedPageId, new AsyncCallback<SubscriptionDTO>() {

            @Override
            public void onSuccess(SubscriptionDTO result) {
                view.updateView(result);
                Chargebee.getInstance().closeAll();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Chargebee.getInstance().closeAll();
                Window.alert("Saving subscription data error: " + caught.getMessage());
            }
            
        });
    }
}
