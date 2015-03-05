package com.sap.sailing.gwt.ui.shared.fakeseries;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.eventview.EventReferenceDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventState;

public class EventSeriesEventDTO extends EventReferenceDTO {
    private Date startDate;
    private Date endDate;
    private String venue;
    private EventState state;

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

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public EventState getState() {
        return state;
    }

    public void setState(EventState state) {
        this.state = state;
    }
}
