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
    private String fileContent;

    public EventPersistenceManager(Context context) {
        this.context = context;
        fileContent = "";
        initializeFile();
    }

    public boolean areIntentsDelayed() {
        return !fileContent.isEmpty();
    }

    public void persistIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        Serializable serializedEvent = extras.getSerializable(AppConstants.EXTRAS_JSON_KEY);
        String raceUuid = extras.getSerializable(AppConstants.RACE_ID_KEY).toString();

        persistEvent(raceUuid, serializedEvent);
    }

    private void persistEvent(String raceUuid, Serializable serializedEvent) {
        String eventLine = String.format("%s;%s\n", raceUuid, serializedEvent);
        ExLog.i(TAG, "Called to persist following event " + eventLine);

        if (!fileContent.isEmpty()) {
            if (fileContent.contains(eventLine)) {
                ExLog.i(TAG, "The event to persist exists already in file");
                return;
            }
        }
        writeEventToFile(eventLine);
    }

    public void removeIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        String serializedEvent = extras.getString(AppConstants.EXTRAS_JSON_KEY);
        String raceUuid = extras.getString(AppConstants.RACE_ID_KEY);

        removeEvent(raceUuid, serializedEvent);
    }

    private void removeEvent(String raceUuid, String serializedEvent) {
        if (fileContent.isEmpty())
            return;

        ExLog.i(TAG, "removeEvent is called for " + raceUuid + ", serializedEvent " + serializedEvent);
        String eventLine = String.format("%s;%s\n", raceUuid, serializedEvent);

        removeEntryInFileContent(eventLine);

    }

    /**
     * @param eventLine
     */
    private void removeEntryInFileContent(String eventLine) {
        if (fileContent.contains(eventLine)) {
            fileContent = fileContent.replace(eventLine + "\n", "");
            writeFileContentToFile();
            ExLog.i(TAG, "Entry " + eventLine + " is removed from fileContent");
        }
    }

    public int getEventCount() {
        if (fileContent.isEmpty()) {
            return 0;
        }
        return fileContent.split("\n").length;
    }

    public List<Intent> restoreEvents() {
        List<Intent> delayedIntents = new ArrayList<Intent>();
        List<String> fileEntriesToBeRemoved = new ArrayList<String>(); //These file entries may be too old. No appropriate race id has been found for this event.

        if (!fileContent.isEmpty()) {
            String[] eventLines = fileContent.split("\n");
            for (String line : eventLines) {
                if (!line.isEmpty()) {
                    String[] lineParts = line.split(";");

                    Intent eventIntent = EventSendingService.createEventIntent(context, lineParts[0], lineParts[1]);
                    if (eventIntent != null) {
                        delayedIntents.add(eventIntent);
                    } else {
                        fileEntriesToBeRemoved.add(line);
                    }
                }
            }
        }
        
        for (String lineToBeRemoved : fileEntriesToBeRemoved) {
            removeEntryInFileContent(lineToBeRemoved);
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

    private void writeEventToFile(String eventLine) {
        fileContent += eventLine;
        writeFileContentToFile();
        ExLog.i(TAG, "Wrote event to file: " + eventLine);
    }

    private void writeFileContentToFile() {
        writeToFile(fileContent, Context.MODE_PRIVATE);
        ExLog.i(TAG, "Wrote file content to file: " + fileContent);
    }

    private void initializeFile() {
        try {
            fileContent = getFileContent();
        } catch (FileNotFoundException e) {
            ExLog.w(TAG, "persistence file not found in internal storage. The file will be created.");
            clearFileContent();
        }
        ExLog.i(TAG, "Initialized file");
    }

    private void clearFileContent() {
        fileContent = "";
        writeFileContentToFile();
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
