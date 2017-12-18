package com.sap.sailing.gwt.ui.adminconsole.resulthandling;


import com.google.gwt.core.client.JavaScriptObject;

public class ErrorMessage extends JavaScriptObject {

    protected ErrorMessage() {
    }

    public final native String getExUUID() /*-{
	return this.exUUID;
    }-*/;
    public final native String getFilename() /*-{
	return this.filename;
    }-*/;
    public final native String getRequestedImporter() /*-{
	return this.requestedImporter;
    }-*/;

    public final native String getClassName() /*-{
	return this.className;
    }-*/;

    public final native String getMessage() /*-{
	return this.message;
    }-*/;

}

