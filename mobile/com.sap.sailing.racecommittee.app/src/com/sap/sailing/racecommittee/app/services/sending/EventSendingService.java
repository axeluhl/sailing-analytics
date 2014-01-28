package com.sap.sailing.racecommittee.app.services.sending;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.sap.sailing.domain.racelog.RaceLogServletConstants;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.receiver.ConnectivityChangedReceiver;
import com.sap.sailing.racecommittee.app.services.sending.EventSenderTask.EventSendingListener;

/*
 * Sending an event to the webservice
 * 
 * Usage:
 * 
 * Intent i = new Intent(AppConstants.SEND_EVENT_ACTION);
 * i.putExtra(AppConstants.EXTRAS_JSON_KEY, JsonUtils.getObjectAsString('someEventObject'));
 * i.putExtra(AppConstants.RACE_UUID_KEY, 'raceuuid');
 * context.startService(i);
 */
public class EventSendingService extends Service implements EventSendingListener {

    protected final static String TAG = EventSendingService.class.getName();

    /**
     * a UUID that identifies this client session; can be used, e.g., to let the server identify subsequent requests coming from the same client
     */
    public final static UUID uuid = UUID.randomUUID();

    private ConnectivityManager connectivityManager;
    private Handler handler;
    private final IBinder mBinder = new EventSendingBinder();
    private EventPersistenceManager persistenceManager;
    private boolean isHandlerSet;
    
    private Set<Serializable> suppressedEventIds = new HashSet<Serializable>();
    
    public void registerEventForSuppression(Serializable eventId) {
        suppressedEventIds.add(eventId);
    }

    private EventSendingServiceLogger serviceLogger = new EventSendingServiceLogger() {
        @Override
        public void onEventSentSuccessful() {
        }

        @Override
        public void onEventSentFailed() {
        }
    };

    public interface EventSendingServiceLogger {
        public void onEventSentSuccessful();

        public void onEventSentFailed();
    }

    public void setEventSendingServiceLogger(EventSendingServiceLogger logger) {
        serviceLogger = logger;
    }

    public class EventSendingBinder extends Binder {
        public EventSendingService getService() {
            return EventSendingService.this;
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
        return persistenceManager.getEventCount();
    }

    public Date getLastSuccessfulSend() {
        return lastSuccessfulSend;
    }
    
    public void clearDelayedIntents() {
        persistenceManager.removeAllEvents();
        // let's fake a successful send!
        serviceLogger.onEventSentSuccessful();
    }

    public static String getRaceLogEventSendAndReceiveUrl(Context context, final String raceGroupName,
            final String raceName, final String fleetName) {
        String url = String.format("%s/sailingserver/rc/racelog?"+
                RaceLogServletConstants.PARAMS_LEADERBOARD_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_RACE_FLEET_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_CLIENT_UUID+"=%s",
                AppPreferences.on(context).getServerBaseURL(), URLEncoder.encode(raceGroupName),
                URLEncoder.encode(raceName), URLEncoder.encode(fleetName), uuid);
        return url;
    }

    /**
     * Creates an intent that contains the event to be sent and the race id which shall be sent to the back end. See
     * constants in <code>AddEntryToRaceLogJsonPostServlet</code> for URL construction rules.
     * 
     * @param context
     *            the context of the app
     * @param race
     *            the race for which the event was created
     * @param serializedEventAsUrlEncodedJson
     *            the event serialized to JSON
     * @param callbackClass
     *            the class of the callback which should process the server reply
     * @return the intent that shall be sent to the EventSendingService
     */
    public static Intent createEventIntent(Context context, ManagedRace race, Serializable eventId, String serializedEventAsJson,
            Class<? extends ServerReplyCallback> callbackClass) {
        String url = getRaceLogEventSendAndReceiveUrl(context, 
                race.getRaceGroup().getName(), race.getName(), race.getFleet().getName());
        return createEventIntent(context, url, race.getId(), eventId, serializedEventAsJson, callbackClass);
    }

    public static Intent createEventIntent(Context context, String url, Serializable raceId, Serializable eventId, String serializedEventAsJson,
            Class<? extends ServerReplyCallback> callbackClass) {
        Intent eventIntent = new Intent(AppConstants.INTENT_ACTION_SEND_EVENT);
        eventIntent.putExtra(AppConstants.RACE_ID_KEY, raceId);
        eventIntent.putExtra(EXTRAS_EVENT_ID, eventId);
        eventIntent.putExtra(AppConstants.EXTRAS_JSON_SERIALIZED_EVENT, serializedEventAsJson);
        eventIntent.putExtra(AppConstants.EXTRAS_URL, url);
        eventIntent.putExtra(AppConstants.EXTRAS_CALLBACK_CLASS, callbackClass == null ? null : callbackClass.getName());
        return eventIntent;
    }
    
    private static final String EXTRAS_EVENT_ID = "_EXTRAS_EVENT_ID";

    private Serializable getEventId(Intent intent) {
        Serializable id = intent.getSerializableExtra(EXTRAS_EVENT_ID);
        if (id != null) {
            return id;
        }
        ExLog.w(TAG, "Unable to extract event identifier from event intent.");
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        persistenceManager = new EventPersistenceManager(this);
        handler = new Handler();
        isHandlerSet = false;
        if (persistenceManager.areIntentsDelayed()) {
            handleDelayedEvents();
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
        if (action.equals(AppConstants.INTENT_ACTION_SEND_SAVED_INTENTS)) {
            handleDelayedEvents();
        } else if (action.equals(AppConstants.INTENT_ACTION_SEND_EVENT)) {
            handleSendEvents(intent);
        }
    }

    private void handleSendEvents(Intent intent) {
        ExLog.i(TAG, String.format("Trying to send an event..."));
        if (!isConnected()) {
            ExLog.i(TAG, String.format("Send aborted because there is no connection."));
            persistenceManager.persistIntent(intent);
            ConnectivityChangedReceiver.enable(this);
            serviceLogger.onEventSentFailed();
        } else {
            sendEvent(intent);
        }
    }

    private void handleDelayedEvents() {
        ExLog.i(TAG, String.format("Trying to resend stored events..."));
        isHandlerSet = false;
        if (!isConnected()) {
            ExLog.i(TAG, String.format("Resend aborted because there is no connection."));
            ConnectivityChangedReceiver.enable(this);
            serviceLogger.onEventSentFailed();
        } else {
            sendDelayedEvents();
        }
    }

    private void sendDelayedEvents() {
        List<Intent> delayedIntents = persistenceManager.restoreEvents();
        ExLog.i(TAG, String.format("Resending %d events...", delayedIntents.size()));
        for (Intent intent : delayedIntents) {
            sendEvent(intent);
        }
    }

    private void sendEvent(Intent intent) {
        if (!AppPreferences.on(this).isSendingActive()) {
            ExLog.i(TAG, "Sending deactivated. Event will not be sent to server.");
        } else {
            Serializable eventId = getEventId(intent);
            if (eventId != null && suppressedEventIds.contains(eventId)) {
                suppressedEventIds.remove(eventId);
                ExLog.i(TAG, String.format("Event %s is suppressed, won't be sent.", eventId));
            } else {
                EventSenderTask task = new EventSenderTask(this);
                task.execute(intent);
            }
        }
    }

    @Override
    public void onEventSent(Intent intent, boolean success, InputStream inputStream) {
        if (!success) {
            ExLog.w(TAG, "Error while posting intent to server. Will persist intent...");
            persistenceManager.persistIntent(intent);
            if (!isHandlerSet) {
                SendDelayedEventsCaller delayedCaller = new SendDelayedEventsCaller(this);
                handler.postDelayed(delayedCaller, AppConstants.EventResendInterval); // after 30 sec, try the sending again
                isHandlerSet = true;
            }
            serviceLogger.onEventSentFailed();
        } else {
            ExLog.i(TAG, "Event successfully sent.");
            if (persistenceManager.areIntentsDelayed()) {
                persistenceManager.removeIntent(intent);
            }
            lastSuccessfulSend = Calendar.getInstance().getTime();
            serviceLogger.onEventSentSuccessful();
            
            String callbackClassString = intent.getStringExtra(AppConstants.EXTRAS_CALLBACK_CLASS);
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
                String raceId = intent.getStringExtra(AppConstants.RACE_ID_KEY);
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
