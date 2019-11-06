package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.sap.sse.gwt.client.xdstorage.StorageMessagingEntryPoint;

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
    /**
     * The path under which the HTML document running the {@link StorageMessagingEntryPoint} can be loaded.
     */
    private static final String MESSAGING_ENTRY_POINT_PATH = "/gwt-base/StorageMessaging.html";

    protected MessagePort() {
        super();
    }
    
    /**
     * Creates a {@link MessagePort} by adding a hidden {@code iframe} element to the {@code body} element of the
     * {@code document} provided, and then {@link #getFromIframe(IFrameElement) obtaining a message port for that
     * iframe}. The {@code iframe} is instructed to load the {@link AbstractMessagingEntryPoint} from the base URL (just
     * protocol/hots/[port] with empty path) provided in the {@code baseUrlForMessagingEntryPoint} parameter.
     * 
     * @param document
     *            the document to whose {@code body} element to append the invisible {@code iframe}
     * @param baseUrlForMessagingEntryPoint
     *            can be provided with or without a trailing slash; during construction of the full URL this method will
     *            ensure that there are no duplications
     * @return a {@link MesssagePort} connected to the content window of a new {@link IFrameElement} that has been added
     *         to the {@code document}'s {@link Document#getBody() body element}.
     */
    public static MessagePort createInDocument(Document document, String baseUrlForMessagingEntryPoint) {
        final IFrameElement iframe = document.createIFrameElement();
        iframe.setAttribute("style", "width:0; height:0; border:0; border:none;");
        // TODO register an onload function on the iframe using JSNI and return the MessagePort to a callback once the onload was triggered
        iframe.setSrc(baseUrlForMessagingEntryPoint+MESSAGING_ENTRY_POINT_PATH);
        Document.get().getBody().appendChild(iframe);
        return getFromIframe(iframe);
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
