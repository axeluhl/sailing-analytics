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
     * Open Chargebee checkout modal {@link https://www.chargebee.com/checkout-portal-docs/api.html#opencheckout}
     * 
     * @param option
     */
    public native void openCheckout(CheckoutOption option);

    /**
     * Close Chargebee checkout modal {@link https://www.chargebee.com/checkout-portal-docs/api.html#closeall}
     */
    public native void closeAll();

    /**
     * Set Chargebee customer portal session
     * {@link https://www.chargebee.com/checkout-portal-docs/api.html#setportalsession} Use this in case we want
     * integrate customer portal in the application
     * 
     * @param sessionSetter
     */
    public native void setPortalSession(PortalSessionSetterCallback sessionSetter);

    /**
     * Create Chargebee customer portal instance
     * {@link https://www.chargebee.com/checkout-portal-docs/api.html#createchargebeeportal}
     * 
     * @return
     */
    public native ChargebeePortal createChargebeePortal();
}
