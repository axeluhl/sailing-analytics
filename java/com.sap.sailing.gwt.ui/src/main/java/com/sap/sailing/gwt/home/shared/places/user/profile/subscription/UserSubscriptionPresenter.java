package com.sap.sailing.gwt.home.shared.places.user.profile.subscription;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sailing.gwt.ui.client.subscription.Chargebee;
import com.sap.sailing.gwt.ui.client.subscription.CheckoutOption;
import com.sap.sailing.gwt.ui.client.subscription.PortalOption;
import com.sap.sailing.gwt.ui.client.subscription.PortalSessionSetterCallback;
import com.sap.sailing.gwt.ui.client.subscription.SubscriptionConfiguration;
import com.sap.sailing.gwt.ui.client.subscription.WithSubscriptionService;
import com.sap.sailing.gwt.ui.shared.subscription.SubscriptionDTO;
import com.sap.sse.security.ui.authentication.WithAuthenticationManager;
import com.sap.sse.security.ui.authentication.WithUserService;

public class UserSubscriptionPresenter<C extends ClientFactoryWithDispatch & ErrorAndBusyClientFactory & WithAuthenticationManager & WithUserService & WithSubscriptionService>
    implements UserSubscriptionView.Presenter {
    
    private final C clientFactory;
    private UserSubscriptionView view;
    
    private CheckoutOption.SuccessCallback onCheckoutSuccessCallback = 
            new CheckoutOption.SuccessCallback() {
        
                @Override
                public void call(String hostedPageId) {
                    requestFinishingPlanUpgrading(hostedPageId);
                }
            };
    private CheckoutOption.ErrorCallback onCheckoutErrorCallback =
            new CheckoutOption.ErrorCallback() {
        
                @Override
                public void call(String error) {
                    Window.alert("Checkout error : " + error);
                }
            };
    private CheckoutOption.CloseCallback onCheckoutCloseCallback =
            new CheckoutOption.CloseCallback() {
        
                @Override
                public void call() {
                    view.onCloseCheckoutModal();
                }
            };
    private PortalOption.CloseCallback onPortalCloseCallback =
            new PortalOption.CloseCallback() {
                
                @Override
                public void call() {
                    view.onClosePortalModal();
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

    @Override
    public void openCheckout() {
        clientFactory.getSubscriptionService().generateHostedPageObject(new AsyncCallback<String>() {
            
            @Override
            public void onSuccess(String hostedPage) {
                Chargebee.getInstance().openCheckout(
                    CheckoutOption.create(
                        hostedPage,
                        onCheckoutSuccessCallback,
                        onCheckoutErrorCallback,
                        onCheckoutCloseCallback
                    ));;
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Checkout error: " + caught.getMessage());
            }
        });
    }
    
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

    @Override
    public void openPortalSession() {
        clientFactory.getSubscriptionService().generatePortalPageObject(new AsyncCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (result == null) {
                    Window.alert("Failed to generating portal page object");
                    return;
                }
                
                Chargebee.getInstance().setPortalSession(PortalSessionSetterCallback.create(result));
                Chargebee.getInstance().createChargebeePortal().open(PortalOption.create(onPortalCloseCallback));
            }
            
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Generating portal page object error: " + caught.getMessage());
            }
        });
        
    }
    
    private void requestFinishingPlanUpgrading(String hostedPageId) {
        clientFactory.getSubscriptionService().upgradePlanSuccess(hostedPageId, new AsyncCallback<SubscriptionDTO>() {

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
