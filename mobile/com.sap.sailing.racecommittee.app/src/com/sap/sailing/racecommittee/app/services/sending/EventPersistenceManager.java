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

        Serializable serializedEvent = extras.getSerializable(AppConstants.EXTRAS_JSON_KEY);
        String raceUuid = extras.getSerializable(AppConstants.RACE_ID_KEY).toString();

        persistEvent(raceUuid, serializedEvent);
    }

    private void persistEvent(String raceUuid, Serializable serializedEvent) {
        String eventLine = getSerializedIntentForPersistence(raceUuid, serializedEvent);
        ExLog.i(TAG, String.format("Persisting event \"%s\" for race %s.", eventLine, raceUuid));

        if (persistedEvents.contains(eventLine)) {
            ExLog.i(TAG, "The event already exists. Ignoring.");
            return;
        }
        saveEvent(eventLine);
    }

    private String getSerializedIntentForPersistence(String raceUuid, Serializable serializedEvent) {
        String eventLine = String.format("%s;%s", raceUuid, serializedEvent);
        return eventLine;
    }

    public void removeIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        String serializedEvent = extras.getString(AppConstants.EXTRAS_JSON_KEY);
        String raceUuid = extras.getString(AppConstants.RACE_ID_KEY);

        removeEvent(raceUuid, serializedEvent);
    }

    private void removeEvent(String raceUuid, String serializedEvent) {
        if (persistedEvents.isEmpty())
            return;

        ExLog.i(TAG,  String.format("Removing event \"%s\" for race %s.", serializedEvent, raceUuid));
        String eventLine = getSerializedIntentForPersistence(raceUuid, serializedEvent);

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
        //These file entries may be too old. No appropriate race id has been found for this event.
        List<String> entriesToBeRemoved = new ArrayList<String>(); 

        for (String persistedEvent : persistedEvents) {

            String[] lineParts = persistedEvent.split(";");

            Intent eventIntent = EventSendingService.createEventIntent(context, lineParts[0], lineParts[1]);
            if (eventIntent != null) {
                delayedIntents.add(eventIntent);
            } else {
                entriesToBeRemoved.add(persistedEvent);
            }
        }

        for (String entryToBeRemoved : entriesToBeRemoved) {
            removePersistedEvent(entryToBeRemoved);
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
