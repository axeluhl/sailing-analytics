package com.sap.sailing.gwt.ui.client.subscription;

import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "Chargebee")
public class ChargebeeInstance {
    public native void openCheckout(CheckoutOption option);
    public native void closeAll();
    public native void setPortalSession(PortalSessionSetterCallback sessionSetter);
    public native ChargebeePortal createChargebeePortal();
}
