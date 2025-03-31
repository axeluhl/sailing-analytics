package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Returned by {@link MessagePort#addMessageListener(MessageListener)}. Use in
 * {@link MessagePort#removeMessageListener(MessageListenerHandle)} to remove that listener again.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class MessageListenerHandle extends JavaScriptObject {
    protected MessageListenerHandle() {
        super();
    }
}
