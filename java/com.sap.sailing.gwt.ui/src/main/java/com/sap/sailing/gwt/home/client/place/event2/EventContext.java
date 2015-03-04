package com.sap.sailing.gwt.home.client.place.event2;

import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

/**
 * Common context used by the different tabs in the event place.
 * 
 * @author pgtaboada
 *
 */
public class EventContext {

    private String eventId;
    private String regattaId;

    /**
     * Common state required by all tabs/ places in event
     */
    private EventViewDTO eventDTO;


    public EventContext() {
    }

    public EventContext(EventViewDTO dto) {
        updateContext(dto);
    }

    public EventContext(EventContext ctx) {
        updateContext(ctx.getEventDTO());
        withRegattaId(ctx.regattaId);
    }

    public EventContext withId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public EventContext withRegattaId(String regattaId) {
        this.regattaId = regattaId;
        return this;
    }


    /**
     * Used to update context with dto instance
     * 
     * @param dto
     * @return
     */
    public EventContext updateContext(EventViewDTO dto) {
        this.eventDTO = dto;
        if (eventDTO == null) {
            withId(null);
        } else {
            withId(dto.id.toString());
        }
        return this;
    }

    public EventViewDTO getEventDTO() {
        return eventDTO;
    }

    public String getEventId() {
        return eventId;
    }

    public String getRegattaId() {
        if(regattaId != null) {
            return regattaId;
        }
        if(eventDTO != null && (eventDTO.getType() == EventType.SINGLE_REGATTA || eventDTO.getType() == EventType.SERIES_EVENT)) {
            return eventDTO.getLeaderboardGroups().get(0).getLeaderboards().get(0).regattaName;
        }
        return null;
    }

    public RegattaMetadataDTO getRegatta() {
        if(regattaId == null || eventDTO == null) {
            return null;
        }
        for (RegattaMetadataDTO regatta : eventDTO.getRegattas()) {
            if(regattaId.equals(regatta.getId())) {
                return regatta;
            }
        }
        return null;
    }


}
