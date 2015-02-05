package com.sap.sailing.gwt.home.client.place.event2.tabs;

import com.sap.sailing.gwt.ui.shared.EventDTO;

/**
 * Common context used by the different tabs in the event place.
 * 
 * @author pgtaboada
 *
 */
public class EventContext {

    private String eventId;
    private String leaderboardIdAsNameString;
    private EventDTO eventDTO;

    public EventContext() {
    }

    public EventContext(EventDTO dto) {
        updateContext(dto);
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
        withId(dto.id.toString());
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



}
