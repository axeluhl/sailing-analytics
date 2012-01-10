package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.client.ui.FormPanel;
import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringConstants;
import com.sap.sailing.gwt.ui.shared.EventDAO;

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
