package com.sap.sailing.racecommittee.app.services.sending;

import java.io.UnsupportedEncodingException;

import org.json.simple.JSONObject;

import android.app.Service;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogChangedListener;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RaceLogEventsCallback;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceEventSender implements RaceLogChangedListener {
    private static final String TAG = RaceEventSender.class.getName();

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
        ExLog.i(service, TAG, "RaceEventSender.eventAdded: "+event.getShortInfo());
        JSONObject serializedEvent = serializer.serialize(event);
        try {
            service.startService(EventSendingServiceUtil.createEventIntent(service, race, event.getId(),
                    serializedEvent.toJSONString(), RaceLogEventsCallback.class));
        } catch (UnsupportedEncodingException e) {
            ExLog.e(service, TAG, "Could not create racelog event message (unsupported encoding)");
        }
    }
}
