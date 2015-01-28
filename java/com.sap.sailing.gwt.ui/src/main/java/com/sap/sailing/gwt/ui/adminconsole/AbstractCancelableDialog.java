package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public abstract class AbstractCancelableDialog extends DialogBox {

    protected final SailingServiceAsync sailingService;
    protected final StringMessages stringMessages;
    protected final ErrorReporter errorReporter;
    protected final VerticalPanel mainPanel;

    public AbstractCancelableDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;

        mainPanel = new VerticalPanel();
        setWidget(mainPanel);
    }

    /**
     * Call from subclass - this allows for finishing constructors of subclasses before building the UI.
     */
    protected void setupUi() {
        addMainContent(mainPanel);
        center();
    }

    protected void addMainContent(Panel mainPanel) {
        HorizontalPanel buttonPanel = new HorizontalPanel();
        mainPanel.add(buttonPanel);

        addButtons(buttonPanel);
    }

    protected void addButtons(Panel buttonPanel) {
        Button cancel = new Button(stringMessages.cancel());
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        buttonPanel.add(cancel);
    }
}