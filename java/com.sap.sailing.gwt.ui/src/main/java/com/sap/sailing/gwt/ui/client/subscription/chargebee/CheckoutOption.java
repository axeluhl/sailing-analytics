package com.sap.sailing.gwt.ui.client.subscription.chargebee;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Class represent JS checkout option object {@link ChargebeeInstance#openCheckout(CheckoutOption)}
 * 
 * @author Tu Tran
 */
public class CheckoutOption extends JavaScriptObject {
    protected CheckoutOption() {
    }

    /**
     * Create checkout option JS object
     * 
     * @param hostedPageJSONString
     *            hosted page object encoded in JSON, this value is generated from back-end
     * @param onSuccess
     *            on checkout success callback
     * @param onError
     *            on checkout fail callback
     * @param onClose
     *            on checkout modal close callback
     * @return checkout option object
     */
    public static native CheckoutOption create(String hostedPageJSONString, SuccessCallback onSuccess,
            ErrorCallback onError, CloseCallback onClose) /*-{
        return {
            hostedPage : function() {
                return Promise.resolve(JSON.parse(hostedPageJSONString));
            },
            success : function(hostedPageId) {
                onSuccess.@com.sap.sailing.gwt.ui.client.subscription.CheckoutOption.SuccessCallback::call(Ljava/lang/String;)(hostedPageId);
            },
            error : function(error) {
                onError.@com.sap.sailing.gwt.ui.client.subscription.CheckoutOption.ErrorCallback::call(Ljava/lang/String;)(error.message ? error.message : error);
            },
            close : function() {
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
