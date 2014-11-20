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
    private final Label venueName;
    private final Label description;
    private final Label startDate;
    private final Label endDate;
    private final Label isPublic;
    private final Label officialWebsiteURL;
    private final Label logoImageURL;

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

        Grid grid = new Grid(8, 2);
        vPanel.add(grid);
        
        eventName = createLabelAndValueWidget(grid, 0, stringMessages.eventName(), "NameLabel");
        description = createLabelAndValueWidget(grid, 1, stringMessages.description(), "DescriptionLabel");
        venueName = createLabelAndValueWidget(grid, 2, stringMessages.venue(), "VenueLabel");
        startDate = createLabelAndValueWidget(grid, 3, stringMessages.startDate(), "StartDateLabel");
        endDate = createLabelAndValueWidget(grid, 4, stringMessages.endDate(), "EndDateLabel");
        isPublic = createLabelAndValueWidget(grid, 5, stringMessages.isPublic(), "IsPublicLabel");
        officialWebsiteURL = createLabelAndValueWidget(grid, 6, stringMessages.eventOfficialWebsiteURL(), "OfficialWebsiteURLLabel");
        logoImageURL = createLabelAndValueWidget(grid, 7, stringMessages.eventLogoImageURL(), "LogoImageURLLabel");
        
        initWidget(mainPanel);
    }
    
    private Label createLabelAndValueWidget(Grid grid, int row, String label, String debugId) {
        Label valueLabel = new Label();
        valueLabel.ensureDebugId(debugId);
        grid.setWidget(row , 0, new Label(label + ":"));
        grid.setWidget(row , 1, valueLabel);
        return valueLabel;
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
            venueName.setText(event.venue.getName());
            description.setText(event.getDescription());
            startDate.setText(event.startDate != null ? event.startDate.toString() : "");
            endDate.setText(event.endDate != null ? event.endDate.toString() : "");
            isPublic.setText(String.valueOf(event.isPublic));
            officialWebsiteURL.setText(event.getOfficialWebsiteURL());
            logoImageURL.setText(event.getLogoImageURL());
        }
    }

}
