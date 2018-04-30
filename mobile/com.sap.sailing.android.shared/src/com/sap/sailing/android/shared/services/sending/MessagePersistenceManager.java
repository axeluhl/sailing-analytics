package com.sap.sailing.android.shared.services.sending;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.FileHandlerUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MessagePersistenceManager {

    private final static String TAG = MessagePersistenceManager.class.getSimpleName();

    private final static String delayedMessagesFileName = "delayedMessages.txt";

    protected Context context;
    
    /**
     * A synchronized {@link LinkedHashSet} that holds the messages to be (re-)sent, in the order in
     * which they were received. Pick the first to retrieve the oldest message. Being a set, no two
     * equal messages will be enqueued.
     */
    private Set<String> persistedMessages;
    
    /**
     * When a message has been picked from the {@link #persistenceManager} for re-send, mark this message as
     * being "on its way" until the callback arrives in {@link #onMessageSent(MessageSenderResult)} where
     * it is removed again from this set and---if sending was successful---from the {@link #persistenceManager}.
     * When the next delayed message shall be picked up by some thread, delayed messages that are still on their
     * way are skipped in the {@link #persistenceManager}'s message list.
     */
    private final Set<String> messagesCurrentlyBeingResent = new HashSet<>();
    
    private final MessageRestorer messageRestorer;

    public MessagePersistenceManager(Context context, MessageRestorer messageRestorer) {
        this.context = context;
        persistedMessages = Collections.synchronizedSet(new LinkedHashSet<String>());
        this.messageRestorer = messageRestorer;
        initializeFileAndPersistedMessages();
    }

    public boolean areIntentsDelayed() {
        return !persistedMessages.isEmpty();
    }

    public void persistIntent(Intent intent) throws UnsupportedEncodingException {
        String messageLine = getMessageLine(intent);
        persistMessage(messageLine);
    }

    private String getMessageLine(Intent intent) throws UnsupportedEncodingException {
        Bundle extras = intent.getExtras();
        String callbackPayload = extras.getString(MessageSendingService.CALLBACK_PAYLOAD);
        String url = extras.getString(MessageSendingService.URL);
        String payload = extras.getString(MessageSendingService.PAYLOAD);
        String callbackClass = extras.getString(MessageSendingService.CALLBACK_CLASS);
        String messageLine = getSerializedIntentForPersistence(url, callbackPayload, payload, callbackClass);
        return messageLine;
    }

    private void persistMessage(String messageLine) throws UnsupportedEncodingException {
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
        return String.format("%s;%s;%s;%s", callbackPayload, URLEncoder.encode(payload,
                MessageSendingService.charsetName),
                url, callbackClass);
    }

    public void removeIntent(Intent intent) throws UnsupportedEncodingException {
        removeMessage(intent);
    }

    private void removeMessage(Intent intent) throws UnsupportedEncodingException {
        if (!persistedMessages.isEmpty()) {
            ExLog.i(context, TAG, String.format("Removing message \"%s\".", intent.getExtras().getString(MessageSendingService.PAYLOAD)));
            String messageLine = getMessageLine(intent);
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

    /**
     * Callers that want to iterate over the result need to synchronize on the result
     */
    public Iterable<String> getContent() {
        return persistedMessages;
    }

    public interface MessageRestorer {
        void restoreMessage(Context context, Intent messageIntent);
    }

    private Intent restorePersistedIntent(String persistedMessage) throws UnsupportedEncodingException {
        String[] lineParts = persistedMessage.split(";");
        final Intent messageIntent; 
        if (lineParts == null || lineParts.length < 4) {
            messageIntent = null;
        } else {
            String url = lineParts[2];
            String callbackPayload = lineParts[0];
            String payload = URLDecoder.decode(lineParts[1], MessageSendingService.charsetName);
            String callbackClassString = lineParts[3];
            Class<? extends ServerReplyCallback> callbackClass = null;
            if (!"null".equals(callbackClassString)) {
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
            messageIntent = MessageSendingService.createMessageIntent(context, url, callbackPayload,
                    null, payload, /* isResend */ true, callbackClass);
            if (messageRestorer != null) {
                messageRestorer.restoreMessage(context, messageIntent);
            }
        }
        return messageIntent;
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
            ExLog.w(context, TAG, "In Method getFileContent(): " + e.getClass().getName() + " / " + e.getMessage() + " fileContent is empty");
        }
        return fileContent;
    }

    private void saveMessage(String messageLine) {
        persistedMessages.add(messageLine);
        writeToFile(messageLine+"\n", Context.MODE_APPEND);
        ExLog.i(context, TAG, "Appended message to file: " + messageLine);
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

    /**
     * This object holds a sequence of messages to be (re)sent. This method picks the first one that is not yet
     * on its way to the message receiver and marks it as being on its way.<p>
     * 
     * When the caller has received the response to sending the message---regardless of failure or success---the client
     * must call {@link #sentSuccessfully(Intent)} for the intent returned by this method so that the 
     */
    public Intent restoreFirstDelayedIntentNotUnderwayAndMarkAsUnderway() throws UnsupportedEncodingException {
        Intent result = null;
        synchronized (persistedMessages) {
            for (final Iterator<String> nextMessageIter=persistedMessages.iterator(); nextMessageIter.hasNext(); ) {
                final String nextMessage = nextMessageIter.next();
                if (!messagesCurrentlyBeingResent.contains(nextMessage)) {
                    result = restorePersistedIntent(nextMessage);
                    if (result == null) {
                        ExLog.w(context, TAG, "In method restoreFirstDelayedIntentNotUnderwayAndMarkAsUnderway: message "+
                                    nextMessage+" was not restored into a valid Intent; dropping.");
                        // couldn't be parsed into an intent; remove:
                        nextMessageIter.remove();
                        writePersistedMessagesToFile();
                    } else {
                        messagesCurrentlyBeingResent.add(nextMessage);
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * The intent returned by {@link #restoreFirstDelayedIntentNotUnderwayAndMarkAsUnderway()} has been delivered
     * successfully to the receiver. It can hence be removed from this persistence manager. In particular, it is no
     * longer "underway."
     */
    public void sentSuccessfully(Intent intent) throws UnsupportedEncodingException {
        synchronized (persistedMessages) {
            messagesCurrentlyBeingResent.remove(getMessageLine(intent));
            removeIntent(intent);
        }
    }

    /**
     * The intent was not delivered successfully to the receiver. It is in particular no longer underway but will need
     * to be sent again, therefore is retained or added in the collection of messages to be sent.
     */
    public void sendFailed(Intent intent) throws UnsupportedEncodingException {
        synchronized (persistedMessages) {
            persistIntent(intent);
            messagesCurrentlyBeingResent.remove(getMessageLine(intent));
        }        
    }
}
