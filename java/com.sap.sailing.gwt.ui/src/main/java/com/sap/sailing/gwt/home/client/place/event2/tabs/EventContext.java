package com.sap.sailing.gwt.home.client.place.event2.tabs;

import com.sap.sailing.gwt.home.client.place.events.EventsClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;

/**
 * Common context used by the different tabs in the event place.
 * 
 * @author pgtaboada
 *
 */
public class EventContext {

    private EventsClientFactory clientFactory;
    private String eventId;
    private String leaderboardIdAsNameString;

    /**
     * Common state required by all tabs/ places in event
     */
    private EventDTO eventDTO;


    public EventContext() {
    }

    public EventContext(EventDTO dto) {
        updateContext(dto);
    }

    public EventContext(EventsClientFactory clientFactory, EventContext ctx) {
        updateContext(ctx.getEventDTO());
        this.clientFactory = clientFactory;
    }

    public EventContext withId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public EventContext withLeaderboardName(String leaderboardIdAsNameString) {
        this.leaderboardIdAsNameString = leaderboardIdAsNameString;
        return this;
    }


    /**
     * Used to update context with dto instance
     * 
     * @param dto
     * @return
     */
    public EventContext updateContext(EventDTO dto) {
        this.eventDTO = dto;
        if (eventDTO == null) {
            withId(null);
        } else {
            withId(dto.id.toString());
        }
        return this;
    }

    public EventDTO getEventDTO() {
        return eventDTO;
    }

    public String getEventId() {
        return eventId;
    }

    public String getLeaderboardIdAsNameString() {
        return leaderboardIdAsNameString;
    }

    public EventsClientFactory getClientFactory() {
        return clientFactory;
    }

    public void setClientFactory(EventsClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

}
