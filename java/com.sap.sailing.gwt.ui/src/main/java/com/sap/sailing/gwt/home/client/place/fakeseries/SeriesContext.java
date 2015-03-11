package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;

/**
 * Common context used by the different tabs in the series place.
 * 
 * @author pgtaboada
 *
 */
public class SeriesContext {

    private String seriesId;

    private EventSeriesViewDTO seriesDTO;

    private MediaDTO media;


    public SeriesContext() {
    }

    public SeriesContext(SeriesContext ctx) {
        updateContext(ctx.getSeriesDTO());
        withMedia(ctx.media);
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
    
    public MediaDTO getMedia() {
        return media;
    }

    public SeriesContext withMedia(MediaDTO media) {
        this.media = media;
        return this;
    }
}
