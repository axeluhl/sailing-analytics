package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

/**
 * Common context used by the different tabs in the series place.
 * 
 * @author pgtaboada
 *
 */
public class SeriesContext {

    private String eventId;
    private String regattaId;

    private EventViewDTO eventDTO;


    public SeriesContext() {
    }

    public SeriesContext(EventViewDTO dto) {
        updateContext(dto);
    }

    public SeriesContext(SeriesContext ctx) {
        updateContext(ctx.getEventDTO());
        withRegattaId(ctx.regattaId);
    }

    public SeriesContext withId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public SeriesContext withRegattaId(String regattaId) {
        this.regattaId = regattaId;
        return this;
    }


    /**
     * Used to update context with dto instance
     * 
     * @param dto
     * @return
     */
    public SeriesContext updateContext(EventViewDTO dto) {
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
        return regattaId;
    }



}
