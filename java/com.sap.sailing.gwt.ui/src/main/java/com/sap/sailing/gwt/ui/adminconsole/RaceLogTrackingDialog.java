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

public abstract class RaceLogTrackingDialog extends DialogBox {
    protected final SailingServiceAsync sailingService;
    protected final StringMessages stringMessages;
    protected final ErrorReporter errorReporter;
    protected final String leaderboardName;
    protected final String raceColumnName;
    protected final String fleetName;
    protected Button saveButton;
    protected final boolean editable;
    protected final VerticalPanel mainPanel;
      
    public RaceLogTrackingDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, String leaderboardName, String raceColumnName, String fleetName) {
        this(sailingService, stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName, true);
    }
    
    public RaceLogTrackingDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, String leaderboardName, String raceColumnName, String fleetName, boolean editable) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.editable = editable;
        
        mainPanel = new VerticalPanel();
        setWidget(mainPanel);

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
