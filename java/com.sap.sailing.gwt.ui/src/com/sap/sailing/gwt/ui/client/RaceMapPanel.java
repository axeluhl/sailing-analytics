package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.shared.EventDAO;

public class RaceMapPanel extends FormPanel implements EventDisplayer {
    private final StringConstants stringConstants;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final EventRefresher eventRefresher;
    
    public RaceMapPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.stringConstants = stringConstants;
        this.errorReporter = errorReporter;
        this.eventRefresher = eventRefresher;
        setWidget(new Label("This is the RaceMapPanel"));
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        // TODO Auto-generated method stub
        
    }
}
