package com.sap.sailing.gwt.home.shared.places.imprint.data;

import com.google.gwt.core.client.JavaScriptObject;

public class ImprintData extends JavaScriptObject {
    protected ImprintData() {
    };

    public final native LicenseData[] getLicenses() /*-{
	return this.licenses;
    }-*/;

    public final native ComponentData[] getComponents() /*-{
	return this.components;
    }-*/;


}
