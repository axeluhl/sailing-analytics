package com.sap.sailing.racecommittee.app.services.sending;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.utils.FileHandlerUtils;

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
        Serializable serializedEvent = extras.getSerializable(AppConstants.EXTRAS_SERIALIZED_EVENT);
        
        persistEvent(url, raceId, serializedEvent);
    }

    private void persistEvent(String url, String raceId, Serializable serializedEvent) {
        String eventLine = getSerializedIntentForPersistence(url, raceId, serializedEvent);
        ExLog.i(TAG, String.format("Persisting event \"%s\" for race %s.", eventLine, raceId));

        if (persistedEvents.contains(eventLine)) {
            ExLog.i(TAG, "The event already exists. Ignoring.");
            return;
        }
        saveEvent(eventLine);
    }

    private String getSerializedIntentForPersistence(String url, String raceId, Serializable serializedEvent) {
        String eventLine = String.format("%s;%s;%s", raceId, serializedEvent, url);
        return eventLine;
    }

    public void removeIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        String url = extras.getString(AppConstants.EXTRAS_URL);
        String raceId = extras.getString(AppConstants.RACE_ID_KEY);
        String serializedEvent = extras.getString(AppConstants.EXTRAS_SERIALIZED_EVENT);
        

        removeEvent(url, raceId, serializedEvent);
    }

    private void removeEvent(String url, String raceId, String serializedEvent) {
        if (persistedEvents.isEmpty())
            return;

        ExLog.i(TAG,  String.format("Removing event \"%s\" for race %s.", serializedEvent, raceId));
        String eventLine = getSerializedIntentForPersistence(url, raceId, serializedEvent);

        removePersistedEvent(eventLine);
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

    public List<Intent> restoreEvents() {
        List<Intent> delayedIntents = new ArrayList<Intent>();

        for (String persistedEvent : persistedEvents) {

            String[] lineParts = persistedEvent.split(";");
            String url = lineParts[2];
            String raceId = lineParts[0];
            String serializedEvent = lineParts[1];

            Intent eventIntent = EventSendingService.createEventIntent(context, url, raceId, serializedEvent);
            if (eventIntent != null) {
                delayedIntents.add(eventIntent);
            }
        }

        ExLog.i(TAG, "Restored " + delayedIntents.size() + " events");
        return delayedIntents;
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
            ExLog.e(TAG, "In Method getFileContent(): " + e.getMessage() + " fileContent is empty");
        }
        return fileContent;
    }

    private void saveEvent(String eventLine) {
        persistedEvents.add(eventLine);
        writePersistedEventsToFile();
        ExLog.i(TAG, "Wrote event to file: " + eventLine);
    }

    private void writePersistedEventsToFile() {
        String newFileContent = "";
        for (String persistedEvent : persistedEvents) {
            newFileContent += persistedEvent + "\n";
        }
        writeToFile(newFileContent, Context.MODE_PRIVATE);
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
