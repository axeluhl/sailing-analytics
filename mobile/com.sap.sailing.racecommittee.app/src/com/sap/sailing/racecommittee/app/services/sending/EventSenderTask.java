package com.sap.sailing.racecommittee.app.services.sending;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.http.HttpJsonPostRequest;
import com.sap.sailing.racecommittee.app.data.http.HttpRequest;
import com.sap.sailing.racecommittee.app.domain.impl.DomainFactoryImpl;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;

public class EventSenderTask extends AsyncTask<Intent, Void, Triple<Intent, Boolean, Iterable<RaceLogEvent>>> {
    
    private static String TAG = EventSenderTask.class.getName();

    public interface EventSendingListener {
        public void onResult(Intent intent, boolean success, Iterable<RaceLogEvent> eventsToAddToRaceLog);
    }

    private EventSendingListener listener;

    public EventSenderTask(EventSendingListener listener) {
        this.listener = listener;
    }

    @Override
    protected Triple<Intent, Boolean, Iterable<RaceLogEvent>> doInBackground(Intent... params) {
        Triple<Intent, Boolean, Iterable<RaceLogEvent>> result;
        Intent intent = params[0];
        if (intent == null) {
            return new Triple<Intent, Boolean, Iterable<RaceLogEvent>>(intent, false, null);
        }
        Bundle extras = intent.getExtras();
        String serializedEventAsJson = extras.getString(AppConstants.EXTRAS_JSON_SERIALIZED_EVENT);
        String url = extras.getString(AppConstants.EXTRAS_URL);
        if (serializedEventAsJson == null || url == null) {
            return new Triple<Intent, Boolean, Iterable<RaceLogEvent>>(intent, false, null);
        }
        final List<RaceLogEvent> eventsToAdd = new ArrayList<RaceLogEvent>();
        try {
            ExLog.i(TAG, "Posting event: " + serializedEventAsJson);
            HttpRequest post = new HttpJsonPostRequest(new URL(url), serializedEventAsJson);
            SharedDomainFactory domainFactory = DomainFactoryImpl.INSTANCE;
            // TODO read JSON-serialized race log events that need to be merged into the local race log because they
            // were added on the server in the interim
            final InputStream inputStream = post.execute();
            JSONParser parser = new JSONParser();
            JSONArray eventsToAddAsJson = (JSONArray) parser.parse(new InputStreamReader(inputStream));
            for (Object o : eventsToAddAsJson) {
                RaceLogEvent eventToAdd = RaceLogEventDeserializer.create(domainFactory).deserialize((JSONObject) o);
                eventsToAdd.add(eventToAdd);
            }
            inputStream.close();
            ExLog.i(TAG, "Post successful for the following event: " + serializedEventAsJson);
            result = new Triple<Intent, Boolean, Iterable<RaceLogEvent>>(intent, true, eventsToAdd);
        } catch (Exception e) {
            ExLog.e(TAG, String.format("Post not successful, exception occured: %s", e.toString()));
            result = new Triple<Intent, Boolean, Iterable<RaceLogEvent>>(intent, false, eventsToAdd);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Triple<Intent, Boolean, Iterable<RaceLogEvent>> resultTriple) {
        super.onPostExecute(resultTriple);
        listener.onResult(resultTriple.getA(), resultTriple.getB(), resultTriple.getC());
    }

}
