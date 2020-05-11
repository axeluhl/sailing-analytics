package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.core.client.JavaScriptObject;

public class PortalOption extends JavaScriptObject {
    protected PortalOption() {}
    
    public static native PortalOption create(CloseCallback onClose) /*-{
        return {
            close: function() {
                onClose.@com.sap.sailing.gwt.ui.client.subscription.PortalOption.CloseCallback::call()();
            }
        };
    }-*/;
    
    public static interface CloseCallback {
        void call();
    }
}
