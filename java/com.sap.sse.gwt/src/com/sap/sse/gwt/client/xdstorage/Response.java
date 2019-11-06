package com.sap.sse.gwt.client.xdstorage;

public class Response extends JavaScriptObjectWithID {
    static final String RESULT = "r";

    protected Response() {
        super();
    }

    public final native Object getResult() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.Response::RESULT];
    }-*/;
}
