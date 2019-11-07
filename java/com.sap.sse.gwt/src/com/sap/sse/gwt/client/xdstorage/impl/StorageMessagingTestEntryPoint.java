package com.sap.sse.gwt.client.xdstorage.impl;

import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;

/**
 * A test application registered at {@code /gwt-base/MessagingTest.html}. When run on, say,
 * {@code http://127.0.0.1:8888}, an {@code iframe} will be loaded from
 * {@code http://localhost:8888/gwt-base/StorageMessaging.html} which will register a
 * {@link LocalStorageDrivenByMessageEvents} message listener which plays the "server" counterpart for
 * {@link CrossDomainStorageImpl}. A few UI elements in the root panel allow the user to get/set/clear/count/list the
 * items in the local storage. Check our your browser's developer tools to introspect the local storage situation. You
 * will see entries for both, {@code http://127.0.0.1:8888} <em>and</em> {@code http://localhost:8888}, with the values
 * submitted through 127.0.0.1 ending up in the localhost section, as intended.
 * <p>
 * 
 * Use the boolean {@code local} URL parameter and set it to {@code true} to choose a local instead of a cross-domain
 * storage implementation. Observe the difference in your browser's development tools.
 * 
 * @see CrossDomainStorage
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class StorageMessagingTestEntryPoint implements EntryPoint {
    @Override
    public void onModuleLoad() {
        final String local = Window.Location.getParameter("local");
        CrossDomainStorage xdStorage = Boolean.valueOf(local) ? CrossDomainStorage.createLocal() : CrossDomainStorage.create("http://localhost:8888");
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