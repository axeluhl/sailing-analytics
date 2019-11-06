package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.JavaScriptObject;

public interface MessageListener<T> {
    default void onMessageReceived(JavaScriptObject jsMessageEvent) {
        @SuppressWarnings("unchecked")
        final MessageEvent<T> messageEvent = (MessageEvent<T>) jsMessageEvent.cast();
        this.onMessageReceived(messageEvent);
    }
    
    void onMessageReceived(MessageEvent<T> messageEvent);
}
