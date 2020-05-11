package com.sap.sailing.gwt.ui.client.subscription;

import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "Chargebee")
public class ChargebeePortal {
    public native void open(PortalOption option);
}
