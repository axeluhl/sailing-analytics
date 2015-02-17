package com.sap.sailing.gwt.home.client.place.event2.tabs;

import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;

/**
 * Common context used by the different tabs in the event place.
 * 
 * @author pgtaboada
 *
 */
public class EventContext {

    private EventClientFactory clientFactory;
    private String eventId;
    private String regattaId;
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

    public EventContext(EventClientFactory clientFactory, EventContext ctx) {
        updateContext(ctx.getEventDTO());
        this.clientFactory = clientFactory;
    }

    public EventContext withId(String eventId) {
        this.eventId = eventId;
        return this;
    }
    
    public EventContext withRegattaId(String regattaId) {
        this.regattaId = regattaId;
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

    public EventClientFactory getClientFactory() {
        return clientFactory;
    }

    public void setClientFactory(EventClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

}
