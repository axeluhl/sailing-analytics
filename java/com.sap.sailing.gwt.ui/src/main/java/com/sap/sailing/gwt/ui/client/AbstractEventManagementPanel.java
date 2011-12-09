package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.client.ui.FormPanel;
import com.sap.sailing.gwt.ui.shared.EventDAO;

public abstract class AbstractEventManagementPanel extends FormPanel implements EventDisplayer{
    protected final SailingServiceAsync sailingService;
    protected DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    protected DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));
    protected final TrackedEventsComposite trackedEventsComposite;
    protected final EventRefresher eventRefresher;
    protected ErrorReporter errorReporter;
    protected StringConstants stringConstants;
    
    public AbstractEventManagementPanel(SailingServiceAsync sailingService,
            EventRefresher eventRefresher, ErrorReporter errorReporter, StringConstants stringConstants) {
        super();
        this.sailingService = sailingService;
        this.eventRefresher = eventRefresher;
        this.errorReporter  = errorReporter;
        this.stringConstants = stringConstants;
        // TrackedEventsComposite should exist in every *ManagementPanel. 
        trackedEventsComposite = new TrackedEventsComposite(sailingService, errorReporter, eventRefresher,
                stringConstants, /* multiselection */ true);
    }

    @Override
    public abstract void fillEvents(List<EventDAO> result);
    
}
