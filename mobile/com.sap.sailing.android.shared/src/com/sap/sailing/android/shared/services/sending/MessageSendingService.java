package com.sap.sailing.android.shared.services.sending;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSenderTask.MessageSendingListener;
import com.sap.sailing.android.shared.util.SharedAppConstants;
import com.sap.sailing.android.shared.util.SharedAppPreferences;

/*
 * Sending a message to the webservice
 * 
 * Usage:
 * 
 * Intent i = new Intent(SharedAppConstants.SEND_MESSAGE_ACTION);
 * i.putExtra(SharedAppConstants.PAYLOAD, JsonUtils.getObjectAsString('someEventObject'));
 * i.putExtra(SharedAppConstants.CALLBACK_PAYLOAD, 'raceuuid');
 * context.startService(i);
 */
public abstract class MessageSendingService extends Service implements MessageSendingListener {

    protected final static String TAG = MessageSendingService.class.getName();

    private ConnectivityManager connectivityManager;
    private Handler handler;
    private final IBinder mBinder = new MessageSendingBinder();
    private MessagePersistenceManager persistenceManager;
    private boolean isHandlerSet;
    
    private Set<Serializable> suppressedMessageIds = new HashSet<Serializable>();
    
    public void registerMessageForSuppression(Serializable messageId) {
        suppressedMessageIds.add(messageId);
    }

    private MessageSendingServiceLogger serviceLogger = new MessageSendingServiceLogger() {
        @Override
        public void onMessageSentSuccessful() {
        }

        @Override
        public void onMessageSentFailed() {
        }
    };

    public interface MessageSendingServiceLogger {
        public void onMessageSentSuccessful();

        public void onMessageSentFailed();
    }

    public void setMessageSendingServiceLogger(MessageSendingServiceLogger logger) {
        serviceLogger = logger;
    }

    public class MessageSendingBinder extends Binder {
        public MessageSendingService getService() {
            return MessageSendingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    private Date lastSuccessfulSend;
    
    public List<String> getDelayedIntensContent() {
        return persistenceManager.getContent();
    }

    public int getDelayedIntentsCount() {
        return persistenceManager.getMessageCount();
    }

    public Date getLastSuccessfulSend() {
        return lastSuccessfulSend;
    }
    
    public void clearDelayedIntents() {
        persistenceManager.removeAllMessages();
        // let's fake a successful send!
        serviceLogger.onMessageSentSuccessful();
    }

    public static Intent createMessageIntent(Context context, String url, Serializable callbackPayload, Serializable messageId, String payload,
            Class<? extends ServerReplyCallback> callbackClass) {
        Intent messageIntent = new Intent(SharedAppConstants.INTENT_ACTION_SEND_MESSAGE);
        messageIntent.putExtra(SharedAppConstants.CALLBACK_PAYLOAD, callbackPayload);
        messageIntent.putExtra(SharedAppConstants.MESSAGE_ID, messageId);
        messageIntent.putExtra(SharedAppConstants.PAYLOAD, payload);
        messageIntent.putExtra(SharedAppConstants.URL, url);
        messageIntent.putExtra(SharedAppConstants.CALLBACK_CLASS, callbackClass == null ? null : callbackClass.getName());
        return messageIntent;
    }

    private Serializable getMessageId(Intent intent) {
        Serializable id = intent.getSerializableExtra(SharedAppConstants.MESSAGE_ID);
        if (id != null) {
            return id;
        }
        ExLog.w(TAG, "Unable to extract message identifier from message intent.");
        return null;
    }
    
    protected abstract MessagePersistenceManager getPersistenceManager();

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        persistenceManager = getPersistenceManager();
        handler = new Handler();
        isHandlerSet = false;
        if (persistenceManager.areIntentsDelayed()) {
            handleDelayedMessages();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            ExLog.i(TAG, "Service is restarted.");
            return START_STICKY;
        }
        ExLog.i(TAG, "Service is called by following intent: " + intent.getAction());
        handleCommand(intent, startId);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void handleCommand(Intent intent, int startId) {
        String action = intent.getAction();
        if (action.equals(SharedAppConstants.INTENT_ACTION_SEND_SAVED_INTENTS)) {
            handleDelayedMessages();
        } else if (action.equals(SharedAppConstants.INTENT_ACTION_SEND_MESSAGE)) {
            handleSendMessages(intent);
        }
    }

    private void handleSendMessages(Intent intent) {
        ExLog.i(TAG, String.format("Trying to send a message..."));
        if (!isConnected()) {
            ExLog.i(TAG, String.format("Send aborted because there is no connection."));
            persistenceManager.persistIntent(intent);
            ConnectivityChangedReceiver.enable(this);
            serviceLogger.onMessageSentFailed();
        } else {
            sendMessage(intent);
        }
    }

    private void handleDelayedMessages() {
        ExLog.i(TAG, String.format("Trying to resend stored messages..."));
        isHandlerSet = false;
        if (!isConnected()) {
            ExLog.i(TAG, String.format("Resend aborted because there is no connection."));
            ConnectivityChangedReceiver.enable(this);
            serviceLogger.onMessageSentFailed();
        } else {
            sendDelayedMessages();
        }
    }

    private void sendDelayedMessages() {
        List<Intent> delayedIntents = persistenceManager.restoreMessages();
        ExLog.i(TAG, String.format("Resending %d messages...", delayedIntents.size()));
        for (Intent intent : delayedIntents) {
            sendMessage(intent);
        }
    }

    private void sendMessage(Intent intent) {
        if (!SharedAppPreferences.on(this).isSendingActive()) {
            ExLog.i(TAG, "Sending deactivated. Message will not be sent to server.");
        } else {
            Serializable messageId = getMessageId(intent);
            if (messageId != null && suppressedMessageIds.contains(messageId)) {
                suppressedMessageIds.remove(messageId);
                ExLog.i(TAG, String.format("Message %s is suppressed, won't be sent.", messageId));
            } else {
                MessageSenderTask task = new MessageSenderTask(this);
                task.execute(intent);
            }
        }
    }

    @Override
    public void onMessageSent(Intent intent, boolean success, InputStream inputStream) {
        if (!success) {
            ExLog.w(TAG, "Error while posting intent to server. Will persist intent...");
            persistenceManager.persistIntent(intent);
            if (!isHandlerSet) {
                SendDelayedMessagesCaller delayedCaller = new SendDelayedMessagesCaller(this);
                handler.postDelayed(delayedCaller, SharedAppConstants.MESSAGE_RESEND_INTERVAL); // after 30 sec, try the sending again
                isHandlerSet = true;
            }
            serviceLogger.onMessageSentFailed();
        } else {
            ExLog.i(TAG, "Message successfully sent.");
            if (persistenceManager.areIntentsDelayed()) {
                persistenceManager.removeIntent(intent);
            }
            lastSuccessfulSend = Calendar.getInstance().getTime();
            serviceLogger.onMessageSentSuccessful();
            
            String callbackClassString = intent.getStringExtra(SharedAppConstants.CALLBACK_CLASS);
            ServerReplyCallback callback = null;
            if (callbackClassString != null) {
                try {
                    callback = (ServerReplyCallback) Class.forName(callbackClassString).newInstance();
                } catch (Exception e) {
                    ExLog.e(TAG, "Error while passing server response to callback");
                    e.printStackTrace();
                }
            }
            if (callback != null) {
                String raceId = intent.getStringExtra(SharedAppConstants.CALLBACK_PAYLOAD);
                callback.processResponse(this, inputStream, raceId);
            }
        }
    }

    /**
     * checks if there is network connectivity
     * 
     * @return connectivity check value
     */
    private boolean isConnected() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }
        return activeNetwork.isConnected();
    }
}
