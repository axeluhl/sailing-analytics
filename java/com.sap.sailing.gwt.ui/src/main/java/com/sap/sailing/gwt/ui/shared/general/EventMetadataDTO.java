package com.sap.sailing.gwt.ui.shared.general;

import java.util.Date;

public class EventMetadataDTO extends EventReferenceDTO {

    private EventState state;
    private String venue;
    private String location;
    private Date startDate;
    private Date endDate;
    private String thumbnailImageURL;

    public EventState getState() {
        return state;
    }
    
    public boolean isStarted() {
        return state.compareTo(EventState.RUNNING) >= 0;
    }
    
    public boolean isFinished() {
        return state == EventState.FINISHED;
    }

    public void setState(EventState state) {
        this.state = state;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getLocation() {
        return location;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getThumbnailImageURL() {
        return thumbnailImageURL;
    }

    public void setThumbnailImageURL(String thumbnailImageURL) {
        this.thumbnailImageURL = thumbnailImageURL;
    }

    public String getLocationOrVenue() {
        if(location != null && !location.isEmpty()) {
            return location;
        }
        return venue;
    }
    
    public String getLocationAndVenue() {
        if(location != null && !location.isEmpty()) {
            return location + ", " + venue;
        }
        return venue;
    }
}
