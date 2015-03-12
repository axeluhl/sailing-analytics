package com.sap.sailing.gwt.ui.shared.general;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventState;

public class EventMetadataDTO extends EventReferenceDTO {

    // private EventType type;
    private EventState state;
    private String venue;
    private String venueCountry;
    private Date startDate;
    private Date endDate;
    private String logoImageURL;

    public EventState getState() {
        return state;
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

    public String getVenueCountry() {
        return venueCountry;
    }

    public void setVenueCountry(String venueCountry) {
        this.venueCountry = venueCountry;
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

    public String getLogoImageURL() {
        return logoImageURL;
    }

    public void setLogoImageURL(String logoImageURL) {
        this.logoImageURL = logoImageURL;
    }

}
