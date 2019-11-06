package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

public class MessagingTestEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        GWT.log("Hello World");
        final MessagePort localPort = MessagePort.getCurrentWindow();
        localPort.addMessageListener(messageEvent -> GWT.log("Received " + messageEvent.getData() + " from origin " + messageEvent.getOrigin()));
        final Button button = new Button("Test");
        final MessagePort globalPort = MessagePort.createInDocument(Document.get(), "http://localhost:8888");
        button.addClickHandler(e -> {
            globalPort.postMessage("Hello iFrame!", "*");
        });
        RootPanel.get().add(button);
    }
}