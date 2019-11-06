package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.IFrameElement;

/**
 * The handle to a {@code Window} in the DOM which can be used to post a message to and to register a message event
 * listener on.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class MessagePort extends JavaScriptObject {
    protected MessagePort() {
        super();
    }
    
    public static final native MessagePort getGlobalWindow() /*-{
        return $wnd;
    }-*/;

    public static final native MessagePort getCurrentWindow() /*-{
        return window;
    }-*/;
    
    public static final native MessagePort getFromIframe(IFrameElement iframe) /*-{
        return iframe.contentWindow;
    }-*/;

    public final native <T> void postMessage(T message, String targetOrigin) /*-{
        this.postMessage(message, targetOrigin);
    }-*/;

    public final native <T> void addMessageListener(MessageListener<T> listener) /*-{
        this.addEventListener("message",
            function(messageEvent) {
                listener.@com.sap.sse.gwt.client.messaging.MessageListener::onMessageReceived(Lcom/google/gwt/core/client/JavaScriptObject;)(messageEvent);
            });
    }-*/;
}
