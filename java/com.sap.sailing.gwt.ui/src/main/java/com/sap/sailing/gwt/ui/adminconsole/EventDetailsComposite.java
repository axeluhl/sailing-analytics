package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class EventDetailsComposite extends Composite {
    private EventDTO event;
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final Label eventName;
    private final CaptionPanel mainPanel;

    public EventDetailsComposite(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        super();
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        
        event = null;
        mainPanel = new CaptionPanel(stringMessages.regatta());
        VerticalPanel vPanel = new VerticalPanel();
        mainPanel.add(vPanel);

        Grid grid = new Grid(6, 2);
        vPanel.add(grid);
        
        eventName = new Label();
        eventName.ensureDebugId("NameLabel");
        grid.setWidget(0 , 0, new Label(stringMessages.eventName() + ":"));
        grid.setWidget(0 , 1, eventName);
        
        initWidget(mainPanel);
    }
    
    public EventDTO getEvent() {
        return event;
    }

    public void setEvent(EventDTO event) {
        this.event = event;
        updateEventDetails();
    }

    private void updateEventDetails() {
        if (event != null) {
            mainPanel.setCaptionText(stringMessages.event() + " " + event.getName());
            eventName.setText(event.getName());
        }
    }

}
