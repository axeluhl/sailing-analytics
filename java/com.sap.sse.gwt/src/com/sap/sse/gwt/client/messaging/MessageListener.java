package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Use together with {@link MessagePort} to register for {@link MessageEvent}s received
 * by the message port.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
@FunctionalInterface
public interface MessageListener<T> {
    default void onMessageReceived(JavaScriptObject jsMessageEvent) {
        @SuppressWarnings("unchecked")
        final MessageEvent<T> messageEvent = (MessageEvent<T>) jsMessageEvent.cast();
        this.onMessageReceived(messageEvent);
    }
    
    void onMessageReceived(MessageEvent<T> messageEvent);
}
