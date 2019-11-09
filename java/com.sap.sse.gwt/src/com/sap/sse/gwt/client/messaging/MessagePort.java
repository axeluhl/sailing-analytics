package com.sap.sse.gwt.client.messaging;

import java.util.function.Consumer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
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
     * Creates a {@link MessagePort} by adding a hidden {@code iframe} element to the {@code body} element of the
     * {@code document} provided, and then {@link #getFromIframe(IFrameElement) obtaining a message port for that
     * iframe}. The {@code iframe} is instructed to load a document that is expected to be implemented by a subclass of
     * {@link AbstractMessagingEntryPoint} from the {@code urlForMessagingEntryPoint} URL.
     * 
     * @param document
     *            the document to whose {@code body} element to append the invisible {@code iframe}
     * @param urlForMessagingEntryPoint
     *            The URL is expected to point to a document rendered by a subclass of
     *            {@link AbstractMessagingEntryPoint}. A protocol between this method and the entry point establishes a
     *            "connection" by the entry point sending a token to its parent window (seen from here that's our
     *            "global window" that we obtain by {@link #getGlobalWindow()}) as its first action after being ready
     *            for incoming messages. This way, the {@link MessagePort} returned by this method will know whether or
     *            not its counter-part is ready for receiving messages.
     * @param resultCallback
     *            a {@link MesssagePort} consumer that is sent a {@link MessagePort} object once the iframe hosting the
     *            window for that message port has announced its readiness, meaning the message port is connected to the content
     *            window of a new {@link IFrameElement} that has been added to the {@code document}'s
     *            {@link Document#getBody() body element}, the contents of the iframe have been loaded, and its
     *            message receiver has been registered on its window properly.
     */
    public static void createInDocument(Document document, String urlForMessagingEntryPoint, Consumer<MessagePort> resultCallback) {
        final IFrameElement iframe = document.createIFrameElement();
        iframe.setAttribute("style", "width:0; height:0; border:0; border:none;");
        iframe.setAttribute("importance", "high"); // shall load as quickly as possible
        Document.get().getBody().appendChild(iframe);
        final MessagePort result = getFromIframe(iframe);
        final MessageListenerHandle[] listenerHandle = new MessageListenerHandle[1];
        final MessagePort iframeParent = getGlobalWindow();
        final MessageListener<String> readyListener = messageEvent->{
            if (messageEvent.getData().equals(AbstractMessagingEntryPoint.READY_TOKEN)) {
                iframeParent.removeMessageListener(listenerHandle[0]);
                resultCallback.accept(result);
            }
        };
        listenerHandle[0] = iframeParent.addMessageListener(readyListener);
        iframe.setSrc(urlForMessagingEntryPoint);
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
     * Returns the {@link MessagePort} view of this port's parent window
     */
    public final native MessagePort getParentWindow() /*-{
        return this.parent.window;
    }-*/;
    
    /**
     * Returns the {@code origin} property of the underlying window.
     */
    public final native String getOrigin() /*-{
        return this.origin;
    }-*/;
    
    /**
     * Uses the {@code iframe}'s content window as the message port.
     */
    public static final native MessagePort getFromIframe(IFrameElement iframe) /*-{
        return iframe.contentWindow;
    }-*/;

    /**
     * Posts a message to this port's {@code window}. All {@link MessagePort} objects for the receiving {@code window}
     * will trigger their registered {@link MessageListener}s accordingly.
     * 
     * @param targetOrigin
     *            can either be "*" which will post the message regardless of the origin of the window sending to (not
     *            recommended for security reasons), or must be a URI providing the same scheme, hostname and port of
     *            the origin of the window sending to. See also
     *            <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage">here</a>
     */
    public final native <T> void postMessage(T message, String targetOrigin) /*-{
        this.postMessage(message, targetOrigin);
    }-*/;

    /**
     * Adds a message listener to this port so that when a message is posted to this port's {@code window}, the
     * listener's {@link MessageListener#onMessageReceived(MessageEvent)} will be invoked.
     */
    public final native <T> MessageListenerHandle addMessageListener(MessageListener<T> listener) /*-{
        var listenerHandle = function(messageEvent) {
                listener.@com.sap.sse.gwt.client.messaging.MessageListener::onMessageReceived(Lcom/google/gwt/core/client/JavaScriptObject;)(messageEvent);
            };
        this.addEventListener("message", listenerHandle);
        return listenerHandle;
    }-*/;

    public final native void removeMessageListener(MessageListenerHandle listenerHandle) /*-{
        this.removeEventListener("message", listenerHandle);
    }-*/;

    /**
     * Adds a listener on this application's {@code window} which for a typical GWT application will be the content
     * window of the implicit {@code iframe} element that GWT generates to host the application. This listener will
     * receive {@link MessageEvent}s that receivers of messages {@link #postMessage(Object, String) posted} through this message port
     * send back to the the {@link MessageEvent#getSource() message event's source}.
     */
    public final <T> MessageListenerHandle addResponseListener(MessageListener<T> listener) {
        return MessagePort.getCurrentWindow().addMessageListener(listener);
    }
}
