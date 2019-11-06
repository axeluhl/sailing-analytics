package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.IFrameElement;

/**
 * The handle to a {@code Window} in the DOM which can be used to post a message to and to register a message event
 * listener on.
 * <p>
 * 
 * A GWT application usually runs within an iframe, creating a somewhat confusing difference between the enclosing HTML
 * page's root window object and the iframe's own content window. Note this difference in how you use the two methods
 * {@link #getGlobalWindow()} and {@link #getCurrentWindow()} to obtain an instance. {@link #getGlobalWindow()} will
 * return the {@link MessagePort} view onto the {@code $wnd} window which is the root window hosting the application,
 * whereas {@link #getCurrentWindow()} returns the content window of the iframe in which the GWT application usually
 * runs.
 * <p>
 * 
 * When used in a GWT application that is truly embedded in another page/window using an iframe, things get even more
 * confusing: there, {@link #getGlobalWindow()} returns the content window of the iframe used to embed the application,
 * whereas {@link #getCurrentWindow()} again uses the content window of the GWT-provided iframe that surrounds a GWT
 * application usually.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class MessagePort extends JavaScriptObject {
    protected MessagePort() {
        super();
    }
    
    /**
     * Uses the {@code $wnd} window handle which for a top-level application represents the enclosing browser window,
     * and for an application embedded by an iframe element represents the iframe's content window.
     */
    public static final native MessagePort getGlobalWindow() /*-{
        return $wnd;
    }-*/;

    /**
     * Uses the {@code window} variable's value which for any regular GWT application is the content window
     * of the iframe that GWT generates implicitly to host the application in. This is <em>not</em> the browser
     * window running the application.
     */
    public static final native MessagePort getCurrentWindow() /*-{
        return window;
    }-*/;
    
    /**
     * Uses the {@code iframe}'s content window as the message port.
     */
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
