package com.sap.sailing.racecommittee.app.services.sending;

import org.json.simple.JSONObject;

import android.app.Service;

import com.sap.sailing.domain.abstractlog.race.RaceLogChangedListener;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RaceLogEventsCallback;
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

    @Override
    public void eventAdded(RaceLogEvent event) {
        JSONObject serializedEvent = serializer.serialize(event);
        service.startService(EventSendingServiceUtil.createEventIntent(service, race, event.getId(),
                serializedEvent.toJSONString(), RaceLogEventsCallback.class));
    }
}
