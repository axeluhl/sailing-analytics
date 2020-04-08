package com.sap.sse.gwt.client.context.data;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Access custom information for GWT client from static browser page.
 * 
 * @see com.sap.sse.gwt.client.context.impl.SapSailingContextDataFactoryImpl
 * @see com.sap.sse.gwt.shared.Branding
 * @author Georg Herdt
 *
 */
public class SapSailingContextDataJSO extends JavaScriptObject {

    protected SapSailingContextDataJSO() {
    }

    public final native boolean isDebrandingActive() /*-{
        return this.debrandingActive;
    }-*/;

}