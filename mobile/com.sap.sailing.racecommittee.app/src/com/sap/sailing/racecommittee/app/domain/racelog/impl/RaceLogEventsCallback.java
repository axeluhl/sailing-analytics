package com.sap.sailing.racecommittee.app.domain.racelog.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.shared.services.sending.MessageSendingService.MessageSendingBinder;
import com.sap.sailing.android.shared.services.sending.ServerReplyCallback;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * <p>
 * Processes (and closes) the {@link InputStream} when posting a {@link RaceLogEvent} to the server.
 * </p>
 * <p>
 * If the server sends back some {@link RaceLogEvent}s the {@link RaceLogEventsCallback} will try to reach the
 * {@link MessageSendingService} and tell him about the server-side {@link RaceLogEvent}s that will be added to the
 * {@link RaceLog}. This gives the {@link MessageSendingService} the chance to ignore them the next time they come
 * around. The execution of the {@link RaceLogEventsCallback} will be deferred until the service is bound.
 * </p>
 * <p>
 * If the {@link MessageSendingService} cannot be reached (this is a really bad sign) the events are added to the
 * {@link RaceLogEvent} immediately.
 * </p>
 */
public class RaceLogEventsCallback implements ServerReplyCallback {
    private static final String TAG = RaceLogEventsCallback.class.getName();

    @Override
    public void processResponse(Context context, InputStream responseStream, String raceId) {
        ReadonlyDataManager dataManager = DataManager.create(context);
        List<RaceLogEvent> eventsToAdd = parseResponse(context, dataManager, responseStream);
        addEvents(context, raceId, dataManager, eventsToAdd);
    }

    protected List<RaceLogEvent> parseResponse(Context context, ReadonlyDataManager dataManager,
            InputStream responseStream) {
        List<RaceLogEvent> eventsToAdd = new ArrayList<RaceLogEvent>();

        JSONParser parser = new JSONParser();
        try {
            JSONArray eventsToAddAsJson = (JSONArray) parser.parse(new InputStreamReader(responseStream));
            SharedDomainFactory domainFactory = dataManager.getDataStore().getDomainFactory();
            RaceLogEventDeserializer deserializer = RaceLogEventDeserializer.create(domainFactory);
            for (Object o : eventsToAddAsJson) {
                try {
                    RaceLogEvent eventToAdd = deserializer.deserialize((JSONObject) o);
                    eventsToAdd.add(eventToAdd);
                } catch (JsonDeserializationException e) {
                    ExLog.e(context, TAG, "Error deserializing Race Log event:\n" + o);
                }
            }
        } catch (Exception e) {
            ExLog.e(context, TAG, "Error parsing server response");
        }

        return eventsToAdd;
    }

    private void addEvents(Context context, String raceId, ReadonlyDataManager dataManager,
            List<RaceLogEvent> eventsToAdd) {
        if (eventsToAdd.isEmpty()) {
            ExLog.i(context, TAG, "No server-side events to add for race " + raceId);
            return;
        }

        ExLog.i(context, TAG,
                String.format("Server sent %d events to be added for race %s.", eventsToAdd.size(), raceId));

        if (!dataManager.getDataStore().hasRace(raceId)) {
            ExLog.w(context, TAG, "I have no race " + raceId);
            return;
        }
        RaceLog raceLog = dataManager.getDataStore().getRace(raceId).getRaceLog();
        if (raceLog == null) {
            ExLog.w(context, TAG, "Unable to retrieve race log for race " + raceId);
            return;
        }

        EventSendingConnection connection = new EventSendingConnection(context, eventsToAdd, raceLog);
        if (context.bindService(new Intent(context, MessageSendingService.class), connection,
                Context.BIND_AUTO_CREATE)) {
            // execution deferred until service is bound
            ExLog.i(context, TAG, "Waiting for sending service to be bound.");
        } else {
            ExLog.e(context, TAG,
                    "Unable to bind to sending service. Processing server response without suppressing received events...");
            addEvents(context, eventsToAdd, raceLog, null);
        }
    }

    protected void addEvents(Context context, List<RaceLogEvent> eventsToAdd, RaceLog raceLog,
            MessageSendingService sendingService) {
        for (RaceLogEvent eventToAddToRaceLog : eventsToAdd) {
            if (sendingService != null) {
                sendingService.registerMessageForSuppression(eventToAddToRaceLog.getId());
            }
            raceLog.add(eventToAddToRaceLog);
            ExLog.i(context, TAG, "Added event " + eventToAddToRaceLog.toString() + " to client's race log");
        }
    }

    /**
     * Connects to the {@link MessageSendingService} and continues with execution. The service will be released
     * afterwards.
     */
    private class EventSendingConnection implements ServiceConnection {

        private final Context context;
        private final List<RaceLogEvent> eventsToAdd;
        private final RaceLog raceLog;
        private MessageSendingService sendingService;

        public EventSendingConnection(Context context, List<RaceLogEvent> eventsToAdd, RaceLog raceLog) {
            this.context = context;
            this.eventsToAdd = eventsToAdd;
            this.raceLog = raceLog;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MessageSendingBinder binder = (MessageSendingBinder) service;
            sendingService = binder.getService();
            ExLog.i(context, TAG, "Sending service is bound. Continue to process server response...");
            addEvents(context, eventsToAdd, raceLog, sendingService);
            context.unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
