package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Class represent JS option object for Chargebee instance openCheckout method
 * {@link ChargebeeInstance}
 * 
 * @author tutran
 */
public class CheckoutOption extends JavaScriptObject {
    protected CheckoutOption() {}
    
    public static native CheckoutOption create(String hostedPage, SuccessCallback onSuccess, ErrorCallback onError, CloseCallback onClose) /*-{
        return {
            hostedPage: function() {
                return Promise.resolve(JSON.parse(hostedPage));
            },
            success: function(hostedPageId) {
                onSuccess.@com.sap.sailing.gwt.ui.client.subscription.CheckoutOption.SuccessCallback::call(Ljava/lang/String;)(hostedPageId);
            },
            error: function(error) {
                onError.@com.sap.sailing.gwt.ui.client.subscription.CheckoutOption.ErrorCallback::call(Ljava/lang/String;)(error.message ? error.message : error);
            },
            close: function() {
                onClose.@com.sap.sailing.gwt.ui.client.subscription.CheckoutOption.CloseCallback::call()();
            }
        };
    }-*/;
    
    public static interface SuccessCallback {
        void call(String hostedPageId);
    }
    
    public static interface ErrorCallback {
        void call(String error);
    }
    
    public static abstract interface CloseCallback {
        void call();
    }
}
