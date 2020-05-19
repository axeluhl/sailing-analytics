package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Class represent JS option object for Chargebee portal instance open method {@link ChargebeePortal}
 * 
 * @author tutran
 */
public class PortalOption extends JavaScriptObject {
    protected PortalOption() {
    }

    public static native PortalOption create(CloseCallback onClose) /*-{
        return {
            close : function() {
                onClose.@com.sap.sailing.gwt.ui.client.subscription.PortalOption.CloseCallback::call()();
            }
        };
    }-*/;

    public static interface CloseCallback {
        void call();
    }
}
