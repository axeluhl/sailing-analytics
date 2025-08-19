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

    public final native String getSailingRaceManagerAppTrimmedImageURL() /*-{
        return this.sailingRaceManagerAppTrimmedImageURL;
    }-*/;

    public final native String getSailingSimulatorTrimmedImageURL() /*-{
        return this.sailingSimulatorTrimmedImageURL;
    }-*/;

    public final native String getSailInSightAppImageURL() /*-{
        return this.sailInSightAppImageURL;
    }-*/;

    public final native String getSailingRaceManagerAppImageURL() /*-{
        return this.sailingRaceManagerAppImageURL;
    }-*/;

    public final native String getSailingSimulatorImageURL() /*-{
        return this.sailingSimulatorImageURL;
    }-*/;

    public final native String getBuoyPingerAppImageURL() /*-{
        return this.buoyPingerAppImageURL;
    }-*/;

    public final native String getSailingAnalyticsImageURL() /*-{
        return this.sailingAnalyticsImageURL;
    }-*/;
}