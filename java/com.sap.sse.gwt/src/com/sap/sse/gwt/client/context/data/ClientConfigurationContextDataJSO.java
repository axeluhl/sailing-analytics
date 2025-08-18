package com.sap.sse.gwt.client.context.data;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Access custom information for GWT client from static browser page.
 * 
 * @see com.sap.sse.gwt.client.context.impl.ClientConfigurationContextDataFactoryImpl
 * @see com.sap.sse.gwt.shared.ClientConfiguration
 * @author Georg Herdt
 *
 */
public class ClientConfigurationContextDataJSO extends JavaScriptObject {
    protected ClientConfigurationContextDataJSO() {
    }

    public final native boolean isDebrandingActive() /*-{
        return this.debrandingActive;
    }-*/;

    public final native String getId() /*-{
        return this.id;
    }-*/;

    public final native String getBrandTitle() /*-{
        return this.brandTitle;
    }-*/;

    public final native String getDefaultBrandingLogoURL() /*-{
        return this.defaultBrandingLogoURL;
    }-*/;

    public final native String getGreyTransparentLogoURL() /*-{
        return this.greyTransparentLogoURL;
    }-*/;

    public final native String getSoutionsInSailingImageURL() /*-{
        return this.solutionsInSailingImageURL;
    }-*/;

    public final native String getSolutionsInSailingTrimmedImageURL() /*-{
        return this.solutionsInSailingTrimmedImageURL;
    }-*/;
}