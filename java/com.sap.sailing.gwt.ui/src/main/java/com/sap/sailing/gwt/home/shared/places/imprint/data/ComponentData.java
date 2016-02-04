package com.sap.sailing.gwt.home.shared.places.imprint.data;

import com.google.gwt.core.client.JavaScriptObject;

public class ComponentData extends JavaScriptObject {
    protected ComponentData() {
    };

    public final native String getName() /*-{
	return this.name;
    }-*/;

    public final native String getVersion() /*-{
	return this.version;
    }-*/;

    public final native String getOwner() /*-{
	return this.owner;
    }-*/;

    public final native String getHomepage() /*-{
	return this.homepage;
    }-*/;

    public final native String[] getAcknowledgements() /*-{
	return this.acknowledgements;
    }-*/;

    public final native String getKey() /*-{
	return this.key;
    }-*/;
}
