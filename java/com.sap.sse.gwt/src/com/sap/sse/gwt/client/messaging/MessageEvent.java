package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.JavaScriptObject;
import com.sap.sse.gwt.client.StorageEvent;

public class MessageEvent<T> extends JavaScriptObject {
    protected MessageEvent() {
        super();
    }
    
    public static interface Handler {
        void onStorageChange(StorageEvent event);
    }

    public final native T getData() /*-{
        return this.data;
    }-*/;

    public final native String getLastEventId() /*-{
        return this.lastEventId;
    }-*/;

    public final native String getOrigin() /*-{
        return this.origin;
    }-*/;

    public final MessagePort getSource() {
        return getWindowSource().cast();
    }

    private final native JavaScriptObject getWindowSource() /*-{
        return this.source;
    }-*/;
}
