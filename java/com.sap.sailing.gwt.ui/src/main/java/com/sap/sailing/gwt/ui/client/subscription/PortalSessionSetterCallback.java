package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.core.client.JavaScriptObject;

public class PortalSessionSetterCallback extends JavaScriptObject {
    protected PortalSessionSetterCallback() {}
    
    public static native PortalSessionSetterCallback create(String portalSession) /*-{
        return function() {
            return new Promise(function (resolve) {
                resolve(JSON.parse(portalSession));
            });
        }
    }-*/;
}
