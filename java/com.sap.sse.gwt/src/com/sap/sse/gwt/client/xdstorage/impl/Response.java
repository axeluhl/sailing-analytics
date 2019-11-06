package com.sap.sse.gwt.client.xdstorage.impl;

public class Response extends JavaScriptObjectWithID {
    static final String RESULT = "r";

    protected Response() {
        super();
    }

    public final native Object getResult() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Response::RESULT];
    }-*/;
}
