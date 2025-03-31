package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Received by a {@link MessagePort} and forwarded to its {@link MessagePort#addMessageListener(MessageListener)
 * registered} {@link MessageListener}s. To respond to the sender, use
 * {@link #getSource()}.{@link MessagePort#postMessage(Object, String) postMessage(Object, String)}.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public class MessageEvent<T> extends JavaScriptObject {
    protected MessageEvent() {
        super();
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
