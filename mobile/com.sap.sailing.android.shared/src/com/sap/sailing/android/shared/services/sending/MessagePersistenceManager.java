package com.sap.sailing.android.shared.services.sending;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.FileHandlerUtils;

public class MessagePersistenceManager {
    
    private final static String TAG = MessagePersistenceManager.class.getName();

    private final static String delayedMessagesFileName = "delayedMessages.txt";

    protected Context context;
    protected List<String> persistedMessages;
    
    private final MessageRestorer messageRestorer;

    public MessagePersistenceManager(Context context, MessageRestorer messageRestorer) {
        this.context = context;
        persistedMessages = new ArrayList<String>();
        this.messageRestorer = messageRestorer;
        initializeFileAndPersistedMessages();
    }

    public boolean areIntentsDelayed() {
        return !persistedMessages.isEmpty();
    }

    public void persistIntent(Intent intent) throws UnsupportedEncodingException {
        Bundle extras = intent.getExtras();
        String callbackPayload = extras.getString(MessageSendingService.CALLBACK_PAYLOAD);
        String url = extras.getString(MessageSendingService.URL);
        String payload = extras.getString(MessageSendingService.PAYLOAD);
        String callbackClass = extras.getString(MessageSendingService.CALLBACK_CLASS);
        persistMessage(url, callbackPayload, payload, callbackClass);
    }

    private void persistMessage(String url, String callbackPayload, String payload, String callbackClass) throws UnsupportedEncodingException {
        String messageLine = getSerializedIntentForPersistence(url, callbackPayload, payload, callbackClass);
        ExLog.i(context, TAG, String.format("Persisting message \"%s\".", messageLine));
        if (persistedMessages.contains(messageLine)) {
            ExLog.i(context, TAG, "The message already exists. Ignoring.");
            return;
        }
        saveMessage(messageLine);
    }

    /**
     * @param payload will be URL-encoded to ensure that the resulting string does not contain newlines
     * @throws UnsupportedEncodingException 
     */
    private String getSerializedIntentForPersistence(String url, String callbackPayload, 
            String payload, String callbackClass) throws UnsupportedEncodingException {
        String messageLine = String.format("%s;%s;%s;%s", callbackPayload, URLEncoder.encode(payload,
                MessageSendingService.charsetName), 
                url, callbackClass);
        return messageLine;
    }

    public void removeIntent(Intent intent) throws UnsupportedEncodingException {
        Bundle extras = intent.getExtras();
        String url = extras.getString(MessageSendingService.URL);
        String callbackPayload = extras.getString(MessageSendingService.CALLBACK_PAYLOAD);
        String payload = extras.getString(MessageSendingService.PAYLOAD);
        String callbackClass = extras.getString(MessageSendingService.CALLBACK_CLASS);
        removeMessage(url, callbackPayload, payload, callbackClass);
    }

    private void removeMessage(String url, String callbackPayload, String payload, String callbackClass) throws UnsupportedEncodingException {
        if (!persistedMessages.isEmpty()) {
            ExLog.i(context, TAG, String.format("Removing message \"%s\".", payload));
            String messageLine = getSerializedIntentForPersistence(url, callbackPayload, payload, callbackClass);
            removePersistedMessage(messageLine);
        }
    }
    
    /**
     * Removes all pending messages and clears the persistence file.
     */
    public synchronized void removeAllMessages() {
        persistedMessages.clear();
        writePersistedMessagesToFile();
    }

    private void removePersistedMessage(String messageLine) {
        if (persistedMessages.contains(messageLine)) {
            persistedMessages.remove(messageLine);
            writePersistedMessagesToFile();
            ExLog.i(context, TAG, "Message removed.");
        }
    }

    public int getMessageCount() {
        return persistedMessages.size();
    }
    
    public List<String> getContent() {
        return persistedMessages;
    }
    
    public static interface MessageRestorer {
        void restoreMessage(Context context, Intent messageIntent);
    }

    public List<Intent> restoreMessages() throws UnsupportedEncodingException {
        List<Intent> delayedIntents = new ArrayList<Intent>();
        for (String persistedMessage : persistedMessages) {
            String[] lineParts = persistedMessage.split(";");
            String url = lineParts[2];
            String callbackPayload = lineParts[0];
            String payload = URLDecoder.decode(lineParts[1], MessageSendingService.charsetName);
            String callbackClassString = lineParts[3];

            Class<? extends ServerReplyCallback> callbackClass = null;
            if (! "null".equals(callbackClassString)) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends ServerReplyCallback> tmp =
                    (Class<? extends ServerReplyCallback>) Class.forName(callbackClassString);
                    callbackClass = tmp;
                } catch (ClassNotFoundException e) {
                    ExLog.e(context, TAG, "Could not find class for callback name: " + callbackClassString);
                }
            }
            
            // We are passing no message id, because we know it used to suppress message sending and
            // we want this message to be sent.
            Intent messageIntent = MessageSendingService.createMessageIntent(context, url, callbackPayload,
                    null, payload, callbackClass);
            
            if (messageRestorer != null) {
                messageRestorer.restoreMessage(context, messageIntent);
            }
            
            if (messageIntent != null) {
                delayedIntents.add(messageIntent);
            }
        }
        ExLog.i(context, TAG, "Restored " + delayedIntents.size() + " messages");
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
            inputStream = context.openFileInput(delayedMessagesFileName);

            fileContent = FileHandlerUtils.convertStreamToString(inputStream, context);
            inputStream.close();
        } catch (IOException e) {
            ExLog.w(context, TAG, "In Method getFileContent(): " + e.getClass().getName()+" / "+e.getMessage() + " fileContent is empty");
        }
        return fileContent;
    }

    private void saveMessage(String messageLine) {
        persistedMessages.add(messageLine);
        writePersistedMessagesToFile();
        ExLog.i(context, TAG, "Wrote message to file: " + messageLine);
    }

    private void writePersistedMessagesToFile() {
        StringBuilder newFileContent = new StringBuilder();
        for (String persistedMessage : persistedMessages) {
            newFileContent.append(persistedMessage);
            newFileContent.append('\n');
        }
        writeToFile(newFileContent.toString(), Context.MODE_PRIVATE);
        ExLog.i(context, TAG, "Wrote file content to file: " + newFileContent);
    }

    private void initializeFileAndPersistedMessages() {
        try {
            String fileContent = getFileContent();
            String[] messageLines = fileContent.split("\n");
            for (String messageLine : messageLines) {
                if (!messageLine.isEmpty()) {
                    persistedMessages.add(messageLine);
                }
            }
        } catch (FileNotFoundException e) {
            ExLog.w(context, TAG, "persistence file not found in internal storage. The file will be created.");
            clearPersistedMessages();
        }
        ExLog.i(context, TAG, "Initialized file");
    }

    private void clearPersistedMessages() {
        persistedMessages.clear();
        writePersistedMessagesToFile();
    }

    private void writeToFile(String content, int mode) {
        try {
            FileOutputStream outputStream = context.openFileOutput(delayedMessagesFileName, mode);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (IOException e) {
            ExLog.e(context, TAG, "In Method writeToFile: " + e.getMessage() + " with content " + content + " and mode " + mode);
        }
    }

}
