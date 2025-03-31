package com.sap.sailing.gwt.home.shared.places.imprint.data;

import com.google.gwt.core.client.JavaScriptObject;

public class LicenseData extends JavaScriptObject {
    protected LicenseData() {
    };

    public final native String getKey() /*-{
	return this.key;
    }-*/;

    public final native String getFile() /*-{
	return this.file;
    }-*/;

}
