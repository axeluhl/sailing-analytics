package com.sap.sailing.racecommittee.app.domain.racelog.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.content.Context;
import android.content.Intent;

import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.services.sending.ServerReplyCallback;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;

public class RaceLogEventsCallback implements ServerReplyCallback {
    private static final String TAG = RaceLogEventsCallback.class.getName();

    @Override
    public void onReply(Intent originalIntent, Context context, InputStream inputStream) {
        ReadonlyDataManager dataManager = DataManager.create(context);
        final List<RaceLogEvent> eventsToAdd = new ArrayList<RaceLogEvent>();
        SharedDomainFactory domainFactory = DataManager.create(context).getDataStore().getDomainFactory();
        
        JSONParser parser = new JSONParser();
        try {
            JSONArray eventsToAddAsJson = (JSONArray) parser.parse(new InputStreamReader(inputStream));
            for (Object o : eventsToAddAsJson) {
                try {
                    RaceLogEvent eventToAdd = RaceLogEventDeserializer.create(domainFactory).deserialize((JSONObject) o);
                    eventsToAdd.add(eventToAdd);
                } catch (JsonDeserializationException e) {
                    ExLog.e(TAG, "Error deserializing Race Log event:\n" + o);
                }
            }
        } catch (Exception e) {
            ExLog.e(TAG, "Error parsing server response");
            ExLog.ex(TAG, e);
        }
        
        String raceId = originalIntent.getStringExtra(AppConstants.RACE_ID_KEY);
        if (dataManager.getDataStore().hasRace(raceId)) {
            RaceLog raceLog = dataManager.getDataStore().getRace(raceId).getRaceLog();
            if (raceLog != null) {
                ExLog.i(TAG, "Successfully retrieved race log for race ID " + raceId);
                for (RaceLogEvent eventToAddToRaceLog : eventsToAdd) {
                    raceLog.add(eventToAddToRaceLog);
                    ExLog.i(TAG, "added event " + eventToAddToRaceLog.toString() + " to client's race log");
                }
            } else {
                ExLog.w(TAG, "Couldn't retrieve race log for race ID " + raceId);
            }
        } else {
            ExLog.w(TAG, "There is no race with id " + raceId);
        }
    }

}
