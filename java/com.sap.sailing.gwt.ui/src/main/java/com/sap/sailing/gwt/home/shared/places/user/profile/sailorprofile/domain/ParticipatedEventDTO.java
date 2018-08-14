package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.domain;

import java.util.Collection;

public class ParticipatedEventDTO {
    private final String eventName;
    private final String eventId;
    private final Collection<ParticipatedRegattaDTO> participatedRegattas;

    public ParticipatedEventDTO(String eventName, String eventId,
            Collection<ParticipatedRegattaDTO> participatedRegattas) {
        super();
        this.eventName = eventName;
        this.eventId = eventId;
        this.participatedRegattas = participatedRegattas;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventId() {
        return eventId;
    }

    public Collection<ParticipatedRegattaDTO> getParticipatedRegattas() {
        return participatedRegattas;
    }

}
