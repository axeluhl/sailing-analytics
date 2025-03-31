package com.sap.sailing.gwt.home.communication.user.profile.domain;

import java.io.Serializable;
import java.util.ArrayList;

import com.sap.sse.common.Util;

/**
 * This object contains the name and id with a fixed set of Participated regattas ({@link ParticipatedRegattaDTO} in a
 * {@link SailorProfileDTO} which is loaded asynchronously.
 */
public class ParticipatedEventDTO implements Serializable {
    private static final long serialVersionUID = -716458570343167392L;

    private String eventName;
    private String eventId;
    private ArrayList<ParticipatedRegattaDTO> participatedRegattas = new ArrayList<>();

    protected ParticipatedEventDTO() {
    }

    public ParticipatedEventDTO(String eventName, String eventId,
            Iterable<ParticipatedRegattaDTO> participatedRegattas) {
        super();
        this.eventName = eventName;
        this.eventId = eventId;
        Util.addAll(participatedRegattas, this.participatedRegattas);
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventId() {
        return eventId;
    }

    public Iterable<ParticipatedRegattaDTO> getParticipatedRegattas() {
        return participatedRegattas;
    }

}
