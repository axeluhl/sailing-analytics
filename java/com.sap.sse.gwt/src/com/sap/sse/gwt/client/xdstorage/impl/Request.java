package com.sap.sse.gwt.client.xdstorage.impl;

public class Request extends JavaScriptObjectWithID {
    static final String OPERATION = "o";
    static final String KEY = "k";
    static final String VALUE = "v";
    static final String INDEX = "i";

    protected Request() {
        super();
    }

    public final native String getOperation() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Request::OPERATION];
    }-*/;

    public final native String getKey() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Request::KEY];
    }-*/;

    public final native String getValue() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Request::VALUE];
    }-*/;

    public final native int getIndex() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Request::INDEX];
    }-*/;
}
