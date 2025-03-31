package com.sap.sailing.gwt.home.shared.places.imprint.data;

import com.google.gwt.core.client.JavaScriptObject;

public class DisclaimerData extends JavaScriptObject {
    protected DisclaimerData() {
    };

    public final native String getTitle() /*-{
	return this.title;
    }-*/;

    public final native String getLink() /*-{
	return this.link;
    }-*/;
    
    public final native String getContent() /*-{
	return this.content;
    }-*/;

}
