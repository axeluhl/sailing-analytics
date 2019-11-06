package com.sap.sse.gwt.client.messaging;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorageImpl;

public class MessagingTestEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        CrossDomainStorage xdStorage = new CrossDomainStorageImpl(Document.get(), "http://localhost:8888");
        final Button button = new Button("Test getLength()");
        RootPanel.get().add(button);
        RootPanel.get().add(new Label("Length: "));
        final Label lengthLabel = new Label();
        RootPanel.get().add(lengthLabel);
        button.addClickHandler(e -> {
            xdStorage.getLength(i->lengthLabel.setText(""+i));
        });
    }
}