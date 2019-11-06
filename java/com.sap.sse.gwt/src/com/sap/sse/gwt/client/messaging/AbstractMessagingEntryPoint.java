package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public abstract class AbstractMessagingEntryPoint<T> implements EntryPoint {
    private MessagePort messagePort;
    
    @Override
    public void onModuleLoad() {
        GWT.log("This is Messaging Entry Point");
        messagePort = MessagePort.getGlobalWindow();
        messagePort.addMessageListener(getMessageListener());
    }

    abstract protected MessageListener<T> getMessageListener();
}
