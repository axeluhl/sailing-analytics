package com.sap.sailing.gwt.home.client.place.event2;

import com.sap.sailing.gwt.ui.shared.eventview.EventMetadataDTO;

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
    private EventMetadataDTO eventDTO;


    public EventContext() {
    }

    public EventContext(EventMetadataDTO dto) {
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
    public EventContext updateContext(EventMetadataDTO dto) {
        this.eventDTO = dto;
        if (eventDTO == null) {
            withId(null);
        } else {
            withId(dto.getId().toString());
        }
        return this;
    }

    public EventMetadataDTO getEventDTO() {
        return eventDTO;
    }

    public String getEventId() {
        return eventId;
    }

    public String getRegattaId() {
        return regattaId;
    }



}
