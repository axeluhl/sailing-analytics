package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public abstract class AbstractSaveDialog extends AbstractCancelableDialog {

    protected Button saveButton;
    protected final boolean editable;

    public AbstractSaveDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean editable) {
        super(sailingService, stringMessages, errorReporter);
        this.editable = editable;
    }

    protected void addButtons(Panel buttonPanel) {
        super.addButtons(buttonPanel);
        if (editable) {
            saveButton = new Button(stringMessages.save());
            saveButton.setTitle(stringMessages.canOnlyBeEditedBeforeStartingTracking());
            saveButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    save();
                }
            });
            buttonPanel.add(saveButton);
        }
    }

    protected abstract void save();

}