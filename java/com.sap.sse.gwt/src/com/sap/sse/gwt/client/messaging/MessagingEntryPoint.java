package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public class MessagingEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        GWT.log("This is Messaging Entry Point");
        final MessagePort messagePort = MessagePort.getGlobalWindow();
        messagePort.addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessageReceived(MessageEvent<String> messageEvent) {
                GWT.log("Received "+messageEvent.getData()+" from origin "+messageEvent.getOrigin());
                messageEvent.getSource().postMessage("Response to "+messageEvent.getData(), "*");
            }
        });
    }
}
