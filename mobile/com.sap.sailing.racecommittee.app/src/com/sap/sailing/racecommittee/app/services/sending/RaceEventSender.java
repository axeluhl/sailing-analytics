package com.sap.sailing.racecommittee.app.services.sending;

import java.io.Serializable;

import android.app.Service;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.racelog.RaceLogChangedListener;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceEventSender implements RaceLogChangedListener {

    private final Service service;
    private final JsonSerializer<RaceLogEvent> serializer;
    private final ManagedRace race;

    public RaceEventSender(Service context, JsonSerializer<RaceLogEvent> serializer, ManagedRace race) {
        this.service = context;
        this.serializer = serializer;
        this.race = race;
    }

    public void eventAdded(RaceLogEvent event) {
        Serializable serializedEvent = serializer.serialize(event);

        service.startService(EventSendingService.createEventIntent(service, race, serializedEvent));
    }

}
