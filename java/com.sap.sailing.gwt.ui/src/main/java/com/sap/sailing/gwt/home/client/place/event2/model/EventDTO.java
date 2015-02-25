package com.sap.sailing.gwt.home.client.place.event2.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventDTO implements IsSerializable {
    private UUID id;
    private String name;
    private State state;
    private EventType type;
    
    private String venue;
    private String venueCountry;
    public Date startDate;
    public Date endDate;
    
    private String logoImageURL;
    private String officialWebsiteURL;
    
    private List<RegattaDTO> regattas = new ArrayList<>();
    private List<EventReferenceDTO> eventsOfSeries = new ArrayList<>();
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public State getState() {
        return state;
    }
    public void setState(State state) {
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
    public String getOfficialWebsiteURL() {
        return officialWebsiteURL;
    }
    public void setOfficialWebsiteURL(String officialWebsiteURL) {
        this.officialWebsiteURL = officialWebsiteURL;
    }
    public List<RegattaDTO> getRegattas() {
        return regattas;
    }
    public void setRegattas(List<RegattaDTO> regattas) {
        this.regattas = regattas;
    }
    public List<EventReferenceDTO> getEventsOfSeries() {
        return eventsOfSeries;
    }
    public void setEventsOfSeries(List<EventReferenceDTO> eventsOfSeries) {
        this.eventsOfSeries = eventsOfSeries;
    }
    public EventType getType() {
        return type;
    }
    public void setType(EventType type) {
        this.type = type;
    }
}
