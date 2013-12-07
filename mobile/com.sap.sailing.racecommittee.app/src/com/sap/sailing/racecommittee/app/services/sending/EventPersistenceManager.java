package com.sap.sailing.racecommittee.app.services.sending;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RaceLogEventsCallback;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.utils.FileHandlerUtils;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;

public class EventPersistenceManager {
    
    private final static String TAG = EventPersistenceManager.class.getName();

    private final static String delayedEventFileName = "delayedEvents.txt";

    private Context context;
    private List<String> persistedEvents;

    public EventPersistenceManager(Context context) {
        this.context = context;
        persistedEvents = new ArrayList<String>();
        initializeFileAndPersistedEvents();
    }

    public boolean areIntentsDelayed() {
        return !persistedEvents.isEmpty();
    }

    public void persistIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String raceId = extras.getString(AppConstants.RACE_ID_KEY);
        String url = extras.getString(AppConstants.EXTRAS_URL);
        String serializedEventAsJson = extras.getString(AppConstants.EXTRAS_JSON_SERIALIZED_EVENT);
        String callbackClass = extras.getString(AppConstants.EXTRAS_CALLBACK_CLASS);
        persistEvent(url, raceId, serializedEventAsJson, callbackClass);
    }

    private void persistEvent(String url, String raceId, String serializedEventAsJson, String callbackClass) {
        String eventLine = getSerializedIntentForPersistence(url, raceId, serializedEventAsJson, callbackClass);
        ExLog.i(TAG, String.format("Persisting event \"%s\" for race %s.", eventLine, raceId));
        if (persistedEvents.contains(eventLine)) {
            ExLog.i(TAG, "The event already exists. Ignoring.");
            return;
        }
        saveEvent(eventLine);
    }

    /**
     * @param serializedEventAsJson will be URL-encoded to ensure that the resulting string does not contain newlines
     */
    private String getSerializedIntentForPersistence(String url, String raceId, 
            String serializedEventAsJson, String callbackClass) {
        String eventLine = String.format("%s;%s;%s;%s", raceId, URLEncoder.encode(serializedEventAsJson.toString()), 
                url, callbackClass);
        return eventLine;
    }

    public void removeIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String url = extras.getString(AppConstants.EXTRAS_URL);
        String raceId = extras.getString(AppConstants.RACE_ID_KEY);
        String serializedEventAsJson = extras.getString(AppConstants.EXTRAS_JSON_SERIALIZED_EVENT);
        String callbackClass = extras.getString(AppConstants.EXTRAS_CALLBACK_CLASS);
        removeEvent(url, raceId, serializedEventAsJson, callbackClass);
    }

    private void removeEvent(String url, String raceId, String serializedEventAsUrlEncodedJson, String callbackClass) {
        if (!persistedEvents.isEmpty()) {
            ExLog.i(TAG, String.format("Removing event \"%s\" for race %s.", serializedEventAsUrlEncodedJson, raceId));
            String eventLine = getSerializedIntentForPersistence(url, raceId, serializedEventAsUrlEncodedJson, callbackClass);
            removePersistedEvent(eventLine);
        }
    }
    
    /**
     * Removes all pending events and clears the persistence file.
     */
    public synchronized void removeAllEvents() {
        persistedEvents.clear();
        writePersistedEventsToFile();
    }

    /**
     * @param eventLine
     */
    private void removePersistedEvent(String eventLine) {
        if (persistedEvents.contains(eventLine)) {
            persistedEvents.remove(eventLine);
            writePersistedEventsToFile();
            ExLog.i(TAG, "Event removed.");
        }
    }

    public int getEventCount() {
        return persistedEvents.size();
    }
    
    public List<String> getContent() {
        return persistedEvents;
    }

    public List<Intent> restoreEvents() {
        List<Intent> delayedIntents = new ArrayList<Intent>();
        for (String persistedEvent : persistedEvents) {
            String[] lineParts = persistedEvent.split(";");
            String url = lineParts[2];
            String raceId = lineParts[0];
            String serializedEventJson = URLDecoder.decode(lineParts[1]);
            String callbackClassString = lineParts[3];
            addEventToLog(raceId, serializedEventJson);

            Class<? extends RaceLogEventsCallback> callbackClass = null;
            if (! "null".equals(callbackClassString)) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends RaceLogEventsCallback> tmp =
                    (Class<? extends RaceLogEventsCallback>) Class.forName(callbackClassString);
                    callbackClass = tmp;
                } catch (ClassNotFoundException e) {
                    ExLog.e(TAG, "Could not find class for callback name: " + callbackClassString);
                }
            }
            
            // We are passing no event id, because we know it used to suppress event sending and
            // we want this event to be sent.
            Intent eventIntent = EventSendingService.createEventIntent(context, url, raceId,
                    null, serializedEventJson, callbackClass);
            if (eventIntent != null) {
                delayedIntents.add(eventIntent);
            }
        }
        ExLog.i(TAG, "Restored " + delayedIntents.size() + " events");
        return delayedIntents;
    }

    private void addEventToLog(String raceId, String serializedEventAsJson) {
        ExLog.i(TAG, String.format("Trying to re-add event to race log of race %s.", raceId));
        DataStore store = DataManager.create(this.context).getDataStore();
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

    /**
     * @return
     * @throws FileNotFoundException
     */
    private String getFileContent() throws FileNotFoundException {
        String fileContent = "";
        FileInputStream inputStream;
        try {
            inputStream = context.openFileInput(delayedEventFileName);

            fileContent = FileHandlerUtils.convertStreamToString(inputStream);
            inputStream.close();
        } catch (IOException e) {
            ExLog.w(TAG, "In Method getFileContent(): " + e.getMessage() + " fileContent is empty");
        }
        return fileContent;
    }

    private void saveEvent(String eventLine) {
        persistedEvents.add(eventLine);
        writePersistedEventsToFile();
        ExLog.i(TAG, "Wrote event to file: " + eventLine);
    }

    private void writePersistedEventsToFile() {
        StringBuilder newFileContent = new StringBuilder();
        for (String persistedEvent : persistedEvents) {
            newFileContent.append(persistedEvent);
            newFileContent.append('\n');
        }
        writeToFile(newFileContent.toString(), Context.MODE_PRIVATE);
        ExLog.i(TAG, "Wrote file content to file: " + newFileContent);
    }

    private void initializeFileAndPersistedEvents() {
        try {
            String fileContent = getFileContent();
            String[] eventLines = fileContent.split("\n");
            for (String eventLine : eventLines) {
                if (!eventLine.isEmpty()) {
                    persistedEvents.add(eventLine);
                }
            }
        } catch (FileNotFoundException e) {
            ExLog.w(TAG, "persistence file not found in internal storage. The file will be created.");
            clearPersistedEvents();
        }
        ExLog.i(TAG, "Initialized file");
    }

    private void clearPersistedEvents() {
        persistedEvents.clear();
        writePersistedEventsToFile();
    }

    private void writeToFile(String content, int mode) {
        try {
            FileOutputStream outputStream = context.openFileOutput(delayedEventFileName, mode);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            ExLog.e(TAG, "In Method writeToFile: " + e.getMessage() + " with content " + content + " and mode " + mode);
        }
    }

}
