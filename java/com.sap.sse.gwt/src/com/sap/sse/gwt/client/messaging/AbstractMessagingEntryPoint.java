package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.EntryPoint;

/**
 * Let entry points inherit from this class and define the {@link #getMessageListener()} method. Then your listener will
 * be attached to the entry point's global window (what GWT returns as {@code $wnd}) and will receive messages posted
 * there.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <T>
 */
public abstract class AbstractMessagingEntryPoint<T> implements EntryPoint {
    private MessagePort messagePort;
    
    @Override
    public void onModuleLoad() {
        messagePort = MessagePort.getGlobalWindow();
        messagePort.addMessageListener(getMessageListener());
    }

    abstract protected MessageListener<T> getMessageListener();
}
