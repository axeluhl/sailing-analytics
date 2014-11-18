package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.EventSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.EventsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * Allows administrators to manage a sailing event.
 * 
 * @author Frank Mittag (C5163974)
 * @author Axel Uhl (d043530)
 */
public class EventManagementPanel extends SimplePanel implements EventsRefresher, LeaderboardGroupsDisplayer, EventSelectionChangeListener {
    private EventListComposite eventListComposite;
    private EventDetailsComposite eventDetailsComposite;
    private final CaptionPanel eventsPanel;
    
    public EventManagementPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");

        eventsPanel = new CaptionPanel(stringMessages.events());
        mainPanel.add(eventsPanel);
        VerticalPanel eventsContentPanel = new VerticalPanel();
        eventsPanel.setContentWidget(eventsContentPanel);
        
        eventListComposite = new EventListComposite(sailingService, errorReporter, stringMessages);
        eventListComposite.ensureDebugId("EventListComposite");
        eventsContentPanel.add(eventListComposite);
        
        eventDetailsComposite = new EventDetailsComposite(sailingService, errorReporter, stringMessages);
        eventDetailsComposite.ensureDebugId("EventDetailsComposite");
        eventDetailsComposite.setVisible(false);
        mainPanel.add(eventDetailsComposite);
    }

    @Override
    public void fillEvents() {
        eventListComposite.fillEvents();
    }

    @Override
    public void fillLeaderboardGroups(Iterable<LeaderboardGroupDTO> leaderboardGroups) {
        eventListComposite.fillLeaderboardGroups(leaderboardGroups);
    }

    @Override
    public void onEventSelectionChange(List<UUID> selectedEvents) {
    }        
}
