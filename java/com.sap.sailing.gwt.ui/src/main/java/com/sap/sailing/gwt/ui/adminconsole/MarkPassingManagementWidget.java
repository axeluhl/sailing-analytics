package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class MarkPassingManagementWidget implements IsWidget {

    protected final Grid mainPanel;

    public MarkPassingManagementWidget(StringMessages stringMessages) {
        mainPanel = new Grid(2, 2);

        Button svButton = new Button(stringMessages.save());

        final CheckBox useSAPMarkPassings = new CheckBox("Use SAP MarkPassing-Algorythm");
        useSAPMarkPassings.setValue(false);
        final Button editButton = new Button("Edit MarkPassings");
        editButton.setEnabled(false);
        useSAPMarkPassings.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (useSAPMarkPassings.getValue()) {
                    editButton.setEnabled(true);
                } else {
                    editButton.setEnabled(false);
                }

            }
        });
        mainPanel.setWidget(0, 0, useSAPMarkPassings);
        mainPanel.setWidget(1, 0, svButton);
        mainPanel.setWidget(1, 1, editButton);
    }
    
    abstract void save();
    
    abstract void refresh();

    @Override
    public Widget asWidget() {
        return mainPanel;
    }
}
