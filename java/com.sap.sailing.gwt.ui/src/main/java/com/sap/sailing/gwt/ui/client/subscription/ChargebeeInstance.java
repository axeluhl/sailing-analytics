package com.sap.sailing.gwt.ui.client.subscription;

import jsinterop.annotations.JsType;

/**
 * Wrapped class for native JS Chargebee's instance
 * 
 * @author tutran
 */
@JsType(isNative = true, namespace = "Chargebee")
public class ChargebeeInstance {
    /**
     * Open checkout modal {@link https://www.chargebee.com/checkout-portal-docs/api.html#opencheckout}
     * 
     * @param option
     *            JS checkout option object
     */
    public native void openCheckout(CheckoutOption option);

    /**
     * Close checkout modal {@link https://www.chargebee.com/checkout-portal-docs/api.html#closeall}
     */
    public native void closeAll();
}
