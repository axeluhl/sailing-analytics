package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public abstract class AbstractOkCancelDialog extends AbstractCancelableDialog {

    protected Button okButton;

    public AbstractOkCancelDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter);
    }

    protected void addButtons(Panel buttonPanel) {
        super.addButtons(buttonPanel);
        okButton = new Button(stringMessages.ok());
        okButton.setTitle(stringMessages.canOnlyBeEditedBeforeStartingTracking());
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ok();
            }
        });
        buttonPanel.add(okButton);
    }

    protected abstract void ok();

}