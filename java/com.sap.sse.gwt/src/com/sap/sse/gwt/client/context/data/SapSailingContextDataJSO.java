package com.sap.sse.gwt.client.context.data;

import com.google.gwt.core.client.JavaScriptObject;

public class SapSailingContextDataJSO extends JavaScriptObject {

    protected SapSailingContextDataJSO() {
    }

    public final native boolean isDebrandingActive() /*-{
        return this.debrandingActive;
    }-*/;

}