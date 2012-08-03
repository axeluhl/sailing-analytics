package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.AbstractRegattaPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractEventManagementPanel extends AbstractRegattaPanel {
    protected final TrackedRacesListComposite trackedRacesListComposite;
    
    public AbstractEventManagementPanel(SailingServiceAsync sailingService, RegattaRefresher regattaRefresher,
            ErrorReporter errorReporter, RaceSelectionProvider raceSelectionProvider, StringMessages stringMessages) {
        super(sailingService, regattaRefresher, errorReporter, stringMessages);
        // TrackedEventsComposite should exist in every *ManagementPanel. 
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, regattaRefresher,
                raceSelectionProvider, stringMessages, /* multiselection */ true);
    }
}
