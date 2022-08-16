package com.sap.sse.gwt.client.xdstorage.impl;

import com.sap.sse.gwt.client.xdstorage.CrossDomainStorageEvent;

/**
 * A message payload sent back by {@link LocalStorageDrivenByMessageEvents} to {@link CrossDomainStorageImpl}, either to
 * identify a response for a specific {@link Request}, keyed by the {@link #getId()} which matches that of the
 * {@link Request#getId() request}, or an encoded {@link CrossDomainStorageEvent} which can be obtained through
 * {@link #getStorageEvent} in case {@link #getId()} is {@code null}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class Response extends JavaScriptObjectWithID {
    static final String RESULT = "r";
    static final String KEY = "k";
    static final String NEW_VALUE = "n";
    static final String OLD_VALUE = "o";
    static final String URL = "u";

    protected Response() {
        super();
    }

    public final native Object getResult() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Response::RESULT];
    }-*/;

    public final native String getKey() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Response::KEY];
    }-*/;

    public final native String getNewValue() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Response::NEW_VALUE];
    }-*/;

    public final native String getOldValue() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Response::OLD_VALUE];
    }-*/;

    public final native String getUrl() /*-{
		return this[@com.sap.sse.gwt.client.xdstorage.impl.Response::URL];
    }-*/;

    public final CrossDomainStorageEvent getStorageEvent() {
        final CrossDomainStorageEvent result;
        if (getResult() != null) {
            result = null;
        } else {
            result = new CrossDomainStorageEventImpl(getKey(), getNewValue(), getOldValue(), getUrl());
        }
        return result;
    }
}
