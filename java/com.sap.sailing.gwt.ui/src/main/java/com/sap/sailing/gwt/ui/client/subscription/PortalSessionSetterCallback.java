package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Class represent Chargeebee portal session callback function {@link ChargebeeInstance#setPortalSession(PortalSessionSetterCallback)}
 * {@link https://www.chargebee.com/checkout-portal-docs/api.html#setportalsession}
 * 
 * @author tutran
 */
public class PortalSessionSetterCallback extends JavaScriptObject {
    protected PortalSessionSetterCallback() {
    }

    public static native PortalSessionSetterCallback create(String portalSession) /*-{
        return function() {
            return new Promise(function(resolve) {
                resolve(JSON.parse(portalSession));
            });
        }
    }-*/;
}
