package com.sap.sailing.gwt.ui.shared.eventview;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class EventViewDTO extends EventMetadataDTO {

    public enum EventType {
        SINGLE_REGATTA, MULTI_REGATTA, SERIES_EVENT
    }

    private ArrayList<RegattaMetadataDTO> regattas = new ArrayList<>();
    private ArrayList<EventReferenceDTO> eventsOfSeries = new ArrayList<>();
    
    private EventType type;
    private boolean hasMedia;
    private boolean hasAnalytics;
    private String seriesName;
    private String logoImageURL;
    private String officialWebsiteURL;

    public EventViewDTO() {
    }

    public EventViewDTO(String name) {
        setDisplayName(name);
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public List<RegattaMetadataDTO> getRegattas() {
        return regattas;
    }

    public List<EventReferenceDTO> getEventsOfSeries() {
        return eventsOfSeries;
    }

    public String getVenueCountry() {
        // FIXME: We need a country?
        return "";
    }
    
    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getSeriesName() {
        return seriesName;
    }
    public String getSeriesIdAsString() {
        return getId().toString();
    }

    public boolean isHasMedia() {
        return hasMedia;
    }

    public void setHasMedia(boolean hasMedia) {
        this.hasMedia = hasMedia;
    }

    public boolean isHasAnalytics() {
        return hasAnalytics;
    }

    public void setHasAnalytics(boolean hasAnalytics) {
        this.hasAnalytics = hasAnalytics;
    }

    public boolean isRegattaIDKnown(String regattaId) {
        for (RegattaMetadataDTO regatta : regattas) {
            if(regatta.getId().equals(regattaId)) {
                return true;
            }
        }
        return false;
    }

    public void setLogoImageURL(String logoImageURL) {
        this.logoImageURL = logoImageURL;
    }
    
    public String getLogoImageURL() {
        return logoImageURL;
    }

    public void setOfficialWebsiteURL(String officialWebsiteURL) {
        this.officialWebsiteURL = officialWebsiteURL;
    }
    
    public String getOfficialWebsiteURL() {
        return officialWebsiteURL;
    }
}
