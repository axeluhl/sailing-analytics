package com.sap.sailing.racecommittee.app.services.sending;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.content.Intent;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessagePersistenceManager.MessageRestorer;
import com.sap.sailing.android.shared.util.SharedAppConstants;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;

public class EventRestorer implements MessageRestorer {
    private final static String TAG = EventRestorer.class.getName();

    @Override
    public void restoreMessage(Context context, Intent messageIntent) {
        String raceId = messageIntent.getExtras().getString(SharedAppConstants.CALLBACK_PAYLOAD);
        String serializedEventAsJson = messageIntent.getExtras().getString(SharedAppConstants.PAYLOAD);
        
        ExLog.i(TAG, String.format("Trying to re-add event to race log of race %s.", raceId));
        DataStore store = DataManager.create(context).getDataStore();
        if (!store.hasRace(raceId)) {
            ExLog.w(TAG, String.format("There is no race %s.", raceId));
            return;
        }
        try {
            RaceLogEventDeserializer deserializer = RaceLogEventDeserializer.create(DataManager.create(context).getDataStore().getDomainFactory());
            RaceLogEvent event = deserializer.deserialize((JSONObject) new JSONParser().parse(serializedEventAsJson));
            boolean added = store.getRace(raceId).getRaceLog().add(event);
            if (added) {
                ExLog.i(TAG, String.format("Event readded: %s", serializedEventAsJson));
            } else {
                ExLog.i(TAG, "Event didn't need to be readded. Same event already in the log");
            }
        } catch (JsonDeserializationException e) {
            ExLog.w(TAG, String.format("Error while readding event to race log: %s", e.toString()));
        } catch (ParseException e) {
            ExLog.w(TAG, String.format("Error while readding event to race log: %s", e.toString()));
        }
    }

}
