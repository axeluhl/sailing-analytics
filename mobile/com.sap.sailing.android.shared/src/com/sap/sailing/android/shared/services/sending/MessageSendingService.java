package com.sap.sailing.android.shared.services.sending;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.sap.sailing.android.shared.R;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessagePersistenceManager.MessageRestorer;
import com.sap.sailing.android.shared.services.sending.MessageSenderTask.MessageSendingListener;
import com.sap.sailing.android.shared.util.PrefUtils;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;

/**
 * Service that handles sending messages to a webservice. Deals with an offline setting
 * by buffering the messages in a file, so that they can be sent when the connection is
 * re-established.<br>
 * 
 * <b>Use in the following way:</b> Add the service declaration to your {@code AndroidManifest.xml},
 * and also specify your class implementing the {@link MessagePersistenceManager.MessageRestorer}
 * as a meta-data tag with the key {@code com.sap.sailing.android.shared.services.sending.messageRestorer}.
 * Also refer to {@link ConnectivityChangedReceiver}, which has to be registered as well.
 * For example:
 * <pre>{@code
 * <service
 *   android:name="com.sap.sailing.android.shared.services.sending.MessageSendingService"
 *   android:exported="false" >
 *   <meta-data android:name="com.sap.sailing.android.shared.services.sending.messageRestorer"
 *     android:value="com.sap.sailing.racecommittee.app.services.sending.EventRestorer" />
 * </service>
 * }</pre>
 * 
 * 
 * Message sending example: 
 * <pre>{@code
 * context.startService(MessageSendingService.createMessageIntent(
 *      context, url, race.getId(), eventId, serializedEventAsJson, callbackClass));
 * }</pre>
 */
public class MessageSendingService extends Service implements MessageSendingListener {
    public final static String URL = "url";
    public final static String PAYLOAD = "payload";
    public final static String CALLBACK_CLASS = "callback";
    public final static String CALLBACK_PAYLOAD = "callbackPayload"; // passed back to callback
    public final static String MESSAGE_ID = "messageId";
    
    public static final String charsetName = "UTF-8";

    protected final static String TAG = MessageSendingService.class.getName();

    private ConnectivityManager connectivityManager;
    private Handler handler;
    private final IBinder mBinder = new MessageSendingBinder();
    private MessagePersistenceManager persistenceManager;
    private boolean isHandlerSet;
    
    private Set<Serializable> suppressedMessageIds = new HashSet<Serializable>();

    private APIConnectivityListener apiConnectivityListener;

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
    
    public List<String> getDelayedIntentsContent() {
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

    public static Intent createMessageIntent(Context context, String url, 
    		Serializable callbackPayload, Serializable messageId, String payload,
            Class<? extends ServerReplyCallback> callbackClass) {
        Intent messageIntent = new Intent(context, MessageSendingService.class);
        messageIntent.setAction(context.getString(R.string.intent_send_message));
        messageIntent.putExtra(CALLBACK_PAYLOAD, callbackPayload);
        messageIntent.putExtra(MESSAGE_ID, messageId);
        messageIntent.putExtra(PAYLOAD, payload);
        messageIntent.putExtra(URL, url);
        messageIntent.putExtra(CALLBACK_CLASS, callbackClass == null ? null : callbackClass.getName());
        return messageIntent;
    }
    
    public static Intent createSendDelayedIntent(Context context) {
        Intent intent = new Intent(context, MessageSendingService.class);
        intent.setAction(context.getString(R.string.intent_send_saved_intents));
        return intent;
    }

    private Serializable getMessageId(Intent intent) {
        Serializable id = intent.getSerializableExtra(MESSAGE_ID);
        if (id != null) {
            return id;
        }
        ExLog.w(this, TAG, "Unable to extract message identifier from message intent.");
        return null;
    }
    
    private MessagePersistenceManager getPersistenceManager() {
        ComponentName thisService = new ComponentName(this, this.getClass());
        MessageRestorer restorer = null;
        try {
            Bundle data = getPackageManager().getServiceInfo(thisService, PackageManager.GET_META_DATA).metaData;
            if (data == null) {
                ExLog.w(this, TAG, "Could not find MessageRestorer. See documentation of MessageSendingService "
                        + "on how to register the restorer through the manifest.");
            } else {
                String className = data.getString("com.sap.sailing.android.shared.services.sending.messageRestorer");
                Class<?> clazz = Class.forName(className);
                if (!MessageRestorer.class.isAssignableFrom(clazz)) {
                    ExLog.w(this, TAG, "Could not find MessageRestorer. See documentation of MessageSendingService "
                            + "on how to register the restorer through the manifest. Class " + clazz
                            + " does not conform to expected type " + MessageRestorer.class.getName());
                } else {
                    @SuppressWarnings("unchecked")
                    // checked above
                    Class<MessageRestorer> castedClass = (Class<MessageRestorer>) clazz;
                    restorer = castedClass.getConstructor().newInstance();
                }
            }
        } catch (Exception e) {
            ExLog.w(this, TAG, "Could not find MessageRestorer. See documentation of MessageSendingService "
                    + "on how to register the restorer through the manifest. Error Message: " + e.getMessage());
        }
        return new MessagePersistenceManager(this, restorer);
    }

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
            ExLog.i(this, TAG, "Service is restarted.");
            return START_STICKY;
        }
        ExLog.i(this, TAG, "Service is called by following intent: " + intent.getAction());
        handleCommand(intent, startId);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void handleCommand(Intent intent, int startId) {
        String action = intent.getAction();
        if (action.equals(getString(R.string.intent_send_saved_intents))) {
            handleDelayedMessages();
        } else if (action.equals(getString(R.string.intent_send_message))) {
            handleSendMessages(intent);
        }
    }

    private void handleSendMessages(Intent intent) {
        ExLog.i(this, TAG, String.format("Trying to send a message..."));
        if (!isConnected()) {
            ExLog.i(this, TAG, String.format("Send aborted because there is no connection."));
            try {
                persistenceManager.persistIntent(intent);
            } catch (UnsupportedEncodingException e) {
                ExLog.e(this, TAG, "Could not persist message (unsupported encoding)");
            }
            ConnectivityChangedReceiver.enable(this);
            serviceLogger.onMessageSentFailed();
            reportApiConnectivity(APIConnectivity.notReachable);
        } else {
            sendMessage(intent);
        }
    }

    private void handleDelayedMessages() {
        ExLog.i(this, TAG, String.format("Trying to resend stored messages..."));
        isHandlerSet = false;
        if (!isConnected()) {
            ExLog.i(this, TAG, String.format("Resend aborted because there is no connection."));
            ConnectivityChangedReceiver.enable(this);
            serviceLogger.onMessageSentFailed();
        } else {
            sendDelayedMessages();
        }
    }

    private void sendDelayedMessages() {
        try {
            List<Intent> delayedIntents = persistenceManager.restoreMessages();
            ExLog.i(this, TAG, String.format("Resending %d messages...", delayedIntents.size()));
            for (Intent intent : delayedIntents) {
                sendMessage(intent);
            }
        } catch (UnsupportedEncodingException e) {
            ExLog.e(this, TAG, "Could not restore messages (unsupported encoding)");
        }
    }

    private void sendMessage(Intent intent) {
        boolean sendingActive = PrefUtils.getBoolean(this, R.string.preference_isSendingActive_key,
                R.bool.preference_isSendingActive_default);
        if (!sendingActive) {
            ExLog.i(this, TAG, "Sending deactivated. Message will not be sent to server.");
        } else {
            Serializable messageId = getMessageId(intent);
            if (messageId != null && suppressedMessageIds.contains(messageId)) {
                suppressedMessageIds.remove(messageId);
                ExLog.i(this, TAG, String.format("Message %s is suppressed, won't be sent.", messageId));
            } else {
                MessageSenderTask task = new MessageSenderTask(this, this);
                task.execute(intent);
            }
        }
    }

    @Override
    public void onMessageSent(Intent intent, boolean success, InputStream inputStream) {
        ExLog.i(this, "MS", "on message sent");
        int resendMillis = PrefUtils.getInt(this, R.string.preference_messageResendIntervalMillis_key,
                R.integer.preference_messageResendIntervalMillis_default);
        if (!success) {
            ExLog.i(this, "MS", "success");
            reportApiConnectivity(APIConnectivity.transmissionError);
            ExLog.w(this, TAG, "Error while posting intent to server. Will persist intent...");
            try {
                persistenceManager.persistIntent(intent);
            } catch (UnsupportedEncodingException e) {
                ExLog.e(this, TAG, "Could not store message (unsupported encoding)");
            }
            if (!isHandlerSet) {
                SendDelayedMessagesCaller delayedCaller = new SendDelayedMessagesCaller(this);
                handler.postDelayed(delayedCaller, resendMillis); // after 30 sec, try the sending again
                isHandlerSet = true;
            }
            reportUnsentGPSFixesCount();
            serviceLogger.onMessageSentFailed();
        } else {
            ExLog.i(this, "MS", "!success");
            reportApiConnectivity(APIConnectivity.transmissionSuccess);
            ExLog.i(this, TAG, "Message successfully sent.");
            if (persistenceManager.areIntentsDelayed()) {
                try {
                    persistenceManager.removeIntent(intent);
                } catch (UnsupportedEncodingException e) {
                    ExLog.e(this, TAG, "Could not remove message (unsupported encoding)");
                }
            }
            lastSuccessfulSend = Calendar.getInstance().getTime();
            serviceLogger.onMessageSentSuccessful();
            
            String callbackClassString = intent.getStringExtra(CALLBACK_CLASS);
            ServerReplyCallback callback = null;
            if (callbackClassString != null) {
                try {
                    callback = (ServerReplyCallback) Class.forName(callbackClassString).newInstance();
                } catch (Exception e) {
                    ExLog.e(this, TAG, "Error while passing server response to callback");
                    e.printStackTrace();
                }
            }
            if (callback != null) {
                String raceId = intent.getStringExtra(CALLBACK_PAYLOAD);
                callback.processResponse(this, inputStream, raceId);
            }
            ExLog.i(this, "MS", "report");
            reportUnsentGPSFixesCount();
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
    
    /**
     * a UUID that identifies this client session; can be used, e.g., to let the server identify subsequent requests coming from the same client
     */
    public final static UUID uuid = UUID.randomUUID();
    
    public static String getRaceLogEventSendAndReceiveUrl(Context context, final String raceGroupName,
            final String raceName, final String fleetName) throws UnsupportedEncodingException {
        String url = String.format("%s/sailingserver/rc/racelog?"+
                RaceLogServletConstants.PARAMS_LEADERBOARD_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_RACE_FLEET_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_CLIENT_UUID+"=%s",
                PrefUtils.getString(context, R.string.preference_server_url_key, R.string.preference_server_url_default),
                URLEncoder.encode(raceGroupName, charsetName),
                URLEncoder.encode(raceName, charsetName), 
				URLEncoder.encode(fleetName, charsetName), uuid);
        return url;
    }

    /**
     * Register listener for API-connectivity
     *
     * @param listener
     *            class that wants to be notified of api-connectivity changes
     */
    public void registerAPIConnectivityListener(APIConnectivityListener listener) {
        apiConnectivityListener = listener;
    }

    /**
     * Unregister listener for API-connectivity
     */
    public void unregisterAPIConnectivityListener() {
        apiConnectivityListener = null;
    }

    /**
     * Enum for reporting of network connectivity.
     */
    public enum APIConnectivity {
        notReachable(0), transmissionSuccess(1), transmissionError(2), noAttempt(4);

        private final int apiConnectivity;

        APIConnectivity(int connectivity) {
            this.apiConnectivity = connectivity;
        }

        public int toInt() {
            return this.apiConnectivity;
        }
    }

    /**
     * Listener interface for reporting of connectivity and number of unsent GPS-fixes.
     */
    public interface APIConnectivityListener {
        public void apiConnectivityUpdated(APIConnectivity apiConnectivity);

        public void setUnsentGPSFixesCount(int count);
    }

    /**
     * Report API connectivity to listening activity
     *
     * @param apiConnectivity
     */
    private void reportApiConnectivity(APIConnectivity apiConnectivity) {
        if (apiConnectivityListener != null) {
            apiConnectivityListener.apiConnectivityUpdated(apiConnectivity);
        }
    }

    /**
     * Report the number of currently unsent GPS-fixes
     *
     * @param unsentGPSFixesCount
     */
    private void reportUnsentGPSFixesCount() {
        if (apiConnectivityListener != null) {
            apiConnectivityListener.setUnsentGPSFixesCount(getDelayedIntentsCount());
        }
    }

    public static String getRacePositionsUrl(Context context, final String regattaName, final String raceName)
            throws UnsupportedEncodingException {
        String url = String
                .format("%ssailingserver/api/v1/regattas/%s/races/%s/marks/positions",// +
                        PrefUtils.getString(context, R.string.preference_server_url_key,
                                R.string.preference_server_url_default), URLEncoder.encode(regattaName, charsetName)
                                .replace("+", "%20"), URLEncoder.encode(raceName, charsetName.replace("+", "%20")));// ,
        return url;
    }
}
