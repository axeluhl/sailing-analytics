package com.sap.sailing.gwt.common.communication.event;

import java.util.UUID;

public class EventSeriesMetadataDTO extends EventSeriesReferenceDTO {
    
    private int eventsCount;
    private String thumbnailImageURL;
    
    protected EventSeriesMetadataDTO() {
    }

    public EventSeriesMetadataDTO(String displayName, UUID seriesLeaderboardGroupId) {
        super(displayName, seriesLeaderboardGroupId);
    }

    public int getEventsCount() {
        return eventsCount;
    }

    public void setEventsCount(int eventsCount) {
        this.eventsCount = eventsCount;
    }

    public String getThumbnailImageURL() {
        return thumbnailImageURL;
    }

    public void setThumbnailImageURL(String thumbnailImageURL) {
        this.thumbnailImageURL = thumbnailImageURL;
    }
    
}
