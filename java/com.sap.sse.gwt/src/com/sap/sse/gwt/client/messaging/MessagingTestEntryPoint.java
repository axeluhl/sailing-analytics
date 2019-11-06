package com.sap.sse.gwt.client.messaging;

import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorageImpl;

public class MessagingTestEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        CrossDomainStorage xdStorage = new CrossDomainStorageImpl(Document.get(), "http://localhost:8888");
        // getLength
        final HorizontalPanel getLengthPanel = new HorizontalPanel();
        RootPanel.get().add(getLengthPanel);
        final Button getLengthButton = new Button("getLength()");
        getLengthPanel.add(getLengthButton);
        getLengthPanel.add(new Label("Length: "));
        final Label lengthLabel = new Label();
        getLengthPanel.add(lengthLabel);
        getLengthButton.addClickHandler(e -> {
            xdStorage.getLength(i->lengthLabel.setText(""+i));
        });
        // clear
        final HorizontalPanel clearPanel = new HorizontalPanel();
        RootPanel.get().add(clearPanel);
        final Button clearButton = new Button("clear()");
        clearPanel.add(clearButton);
        final Label clearLabel = new Label();
        clearPanel.add(clearLabel);
        clearButton.addClickHandler(e -> {
            xdStorage.clear(v->clearLabel.setText("Clear OK at "+new Date()));
        });
        // setItem
        final Button setItemButton = new Button("setItem(");
        final HorizontalPanel setItemPanel = new HorizontalPanel();
        RootPanel.get().add(setItemPanel);
        setItemPanel.add(setItemButton);
        final TextBox keyBox = new TextBox();
        setItemPanel.add(keyBox);
        setItemPanel.add(new Label(", "));
        final TextBox valueBox = new TextBox();
        setItemPanel.add(valueBox);
        setItemPanel.add(new Label(")"));
        setItemButton.addClickHandler(e -> {
            xdStorage.setItem(keyBox.getValue(), valueBox.getValue(),
                    v->{
                        GWT.log("setItem("+keyBox.getValue()+", "+valueBox.getValue()+") went OK");   
                    });
        });
        // getItem
        final Button getItemButton = new Button("getItem(");
        final HorizontalPanel getItemPanel = new HorizontalPanel();
        RootPanel.get().add(getItemPanel);
        getItemPanel.add(getItemButton);
        final TextBox getItemKeyBox = new TextBox();
        getItemPanel.add(getItemKeyBox);
        getItemPanel.add(new Label(")"));
        getItemPanel.add(new Label("Value:"));
        final Label getItemValueLabel = new Label();
        getItemPanel.add(getItemValueLabel);
        getItemButton.addClickHandler(e -> {
            xdStorage.getItem(getItemKeyBox.getValue(),
                    value->getItemValueLabel.setText(value));
        });
        // getAllKeys
        final Button getAllKeysButton = new Button("getAllKeys()");
        final HorizontalPanel getAllKeysPanel = new HorizontalPanel();
        RootPanel.get().add(getAllKeysPanel);
        getAllKeysPanel.add(getAllKeysButton);
        getAllKeysPanel.add(new Label("All Keys:"));
        final Label allKeysLabel = new Label();
        getAllKeysPanel.add(allKeysLabel);
        getAllKeysButton.addClickHandler(e -> {
            xdStorage.getLength(length->{
                allKeysLabel.setText("");
                for (int i=0; i<length; i++) {
                    xdStorage.key(i, value->allKeysLabel.setText((allKeysLabel.getText().isEmpty()?"":allKeysLabel.getText()+", ")+value));
                }
            });
        });
    }
}