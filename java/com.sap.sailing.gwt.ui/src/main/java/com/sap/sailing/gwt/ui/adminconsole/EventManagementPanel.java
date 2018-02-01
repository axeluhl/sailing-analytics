package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.gwt.ui.client.EventsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sse.gwt.adminconsole.HandleTabSelectable;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;

/**
 * Allows administrators to manage a sailing event.
 * 
 * @author Frank Mittag (C5163974)
 * @author Axel Uhl (d043530)
 */
public class EventManagementPanel extends SimplePanel implements EventsRefresher, LeaderboardGroupsDisplayer {
    private EventListComposite eventListComposite;
    private EventDetailsComposite eventDetailsComposite;
    private final CaptionPanel eventsPanel;
    private final RefreshableMultiSelectionModel<EventDTO> refreshableEventSelectionModel;
    
    public EventManagementPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, final StringMessages stringMessages, final HandleTabSelectable handleTabSelectable) {
        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");

        eventsPanel = new CaptionPanel(stringMessages.events());
        mainPanel.add(eventsPanel);
        VerticalPanel eventsContentPanel = new VerticalPanel();
        eventsPanel.setContentWidget(eventsContentPanel);

        eventListComposite = new EventListComposite(sailingService, errorReporter, regattaRefresher, this, handleTabSelectable, stringMessages);
        eventListComposite.ensureDebugId("EventListComposite");
        eventsContentPanel.add(eventListComposite);
        
        eventDetailsComposite = new EventDetailsComposite(sailingService, errorReporter, stringMessages);
        eventDetailsComposite.ensureDebugId("EventDetailsComposite");
        eventDetailsComposite.setVisible(false);
        mainPanel.add(eventDetailsComposite);
        
        refreshableEventSelectionModel = eventListComposite.getRefreshableMultiSelectionModel();
        refreshableEventSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final Set<EventDTO> selectedEvents = refreshableEventSelectionModel.getSelectedSet();
                if (selectedEvents.size() == 1 && eventListComposite.getAllEvents() != null) {
                    final EventDTO selectedEvent = selectedEvents.iterator().next();
                        for (EventDTO eventDTO : eventListComposite.getAllEvents()) {
                            if (eventDTO.id.equals(selectedEvent.id)) {
                                eventDetailsComposite.setEvent(eventDTO);
                                eventDetailsComposite.setVisible(true);
                                break;
                            }
                    }
                } else {
                    eventDetailsComposite.setEvent(null);
                    eventDetailsComposite.setVisible(false);
                }
            }
        });
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
    public void setupLeaderboardGroups(Map<String, String> params) {
        eventListComposite.setupLeaderboardGroups(params);
    }
}
