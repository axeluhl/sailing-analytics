package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

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
    public static final String READY_TOKEN = "I'm ready to rock";
    
    private MessagePort messagePort;
    
    @Override
    public void onModuleLoad() {
        messagePort = MessagePort.getGlobalWindow();
        messagePort.addMessageListener(getMessageListener());
        GWT.log("iframe listener set up");
        messagePort.getParentWindow().postMessage(READY_TOKEN, "*");
        GWT.log("iframe announced readiness");
    }

    abstract protected MessageListener<T> getMessageListener();
}
