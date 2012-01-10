package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringConstants;

public abstract class AbstractEventManagementPanel extends AbstractEventPanel {
    protected final TrackedEventsComposite trackedEventsComposite;
    
    public AbstractEventManagementPanel(SailingServiceAsync sailingService,
            EventRefresher eventRefresher, ErrorReporter errorReporter, StringConstants stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);
        // TrackedEventsComposite should exist in every *ManagementPanel. 
        trackedEventsComposite = new TrackedEventsComposite(sailingService, errorReporter, eventRefresher,
                stringConstants, /* multiselection */ true);
    }
}
