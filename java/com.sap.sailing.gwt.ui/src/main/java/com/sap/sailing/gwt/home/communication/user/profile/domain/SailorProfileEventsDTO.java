package com.sap.sailing.gwt.home.communication.user.profile.domain;

import java.io.Serializable;
import java.util.ArrayList;

import com.sap.sse.common.Util;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

public class SailorProfileEventsDTO implements Serializable, Result {
    private static final long serialVersionUID = 4658596949068331464L;

    private ArrayList<ParticipatedEventDTO> participatedEvents = new ArrayList<>();

    protected SailorProfileEventsDTO() {
    }

    public SailorProfileEventsDTO(Iterable<ParticipatedEventDTO> participatedEvents) {
        Util.addAll(participatedEvents, this.participatedEvents);
    }

    public Iterable<ParticipatedEventDTO> getParticipatedEvents() {
        return participatedEvents;
    }

}
