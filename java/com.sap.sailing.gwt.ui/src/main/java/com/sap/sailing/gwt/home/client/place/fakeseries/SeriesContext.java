package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;

/**
 * Common context used by the different tabs in the series place.
 * 
 * @author pgtaboada
 *
 */
public class SeriesContext {

    private String seriesId;

    private EventSeriesViewDTO seriesDTO;


    public SeriesContext() {
    }

    public SeriesContext(EventSeriesViewDTO dto) {
        updateContext(dto);
    }

    public SeriesContext(SeriesContext ctx) {
        updateContext(ctx.getSeriesDTO());
    }

    public SeriesContext withId(String eventId) {
        this.seriesId = eventId;
        return this;
    }

    /**
     * Used to update context with dto instance
     * 
     * @param dto
     * @return
     */
    public SeriesContext updateContext(EventSeriesViewDTO dto) {
        this.seriesDTO = dto;
        if (seriesDTO == null) {
            withId(null);
        } else {
            withId(dto.getId().toString());
        }
        return this;
    }

    public EventSeriesViewDTO getSeriesDTO() {
        return seriesDTO;
    }

    public String getSeriesId() {
        return seriesId;
    }
}
