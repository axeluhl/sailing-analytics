package com.sap.sailing.gwt.ui.client.subscription;

import jsinterop.annotations.JsType;

/**
 * Wrapped class for Chargebee native JS portal instance
 * 
 * @author tutran
 */
@JsType(isNative = true, namespace = "Chargebee")
public class ChargebeePortal {
    public native void open(PortalOption option);
}
