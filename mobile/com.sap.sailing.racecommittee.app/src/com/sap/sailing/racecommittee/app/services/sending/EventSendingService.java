package com.sap.sailing.racecommittee.app.services.sending;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogServletConstants;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
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

    private ConnectivityManager connectivityManager;
    private Handler handler;
    private final IBinder mBinder = new EventSendingBinder();
    private EventPersistenceManager persistenceManager;
    private ReadonlyDataManager dataManager;
    private boolean isHandlerSet;

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

    /**
     * a UUID that identifies this client session; can be used, e.g., to let the server identify subsequent requests coming from the same client
     */
    private final static UUID uuid = UUID.randomUUID();

    public int getDelayedIntentsCount() {
        return persistenceManager.getEventCount();
    }

    public Date getLastSuccessfulSend() {
        return lastSuccessfulSend;
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
     * @return the intent that shall be sent to the EventSendingService
     */
    public static Intent createEventIntent(Context context, ManagedRace race, String serializedEventAsJson) {
        String url = String.format("%s/sailingserver/rc/racelog?"+
                RaceLogServletConstants.PARAMS_LEADERBOARD_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_RACE_FLEET_NAME+"=%s&"+
                RaceLogServletConstants.PARAMS_CLIENT_UUID+"=%s",
                AppPreferences.getServerBaseURL(context), URLEncoder.encode(race.getRaceGroup().getName()),
                URLEncoder.encode(race.getName()), URLEncoder.encode(race.getFleet().getName()), uuid);
        return createEventIntent(context, url, race.getId(), serializedEventAsJson);
    }

    public static Intent createEventIntent(Context context, String url, Serializable raceId, String serializedEventAsJson) {
        Intent eventIntent = new Intent(AppConstants.INTENT_ACTION_SEND_EVENT);
        eventIntent.putExtra(AppConstants.RACE_ID_KEY, raceId);
        eventIntent.putExtra(AppConstants.EXTRAS_JSON_SERIALIZED_EVENT, serializedEventAsJson);
        eventIntent.putExtra(AppConstants.EXTRAS_URL, url);
        ExLog.i(TAG, "Created event " + eventIntent + " for sending to backend");
        return eventIntent;
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
        dataManager = DataManager.create(this);
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
        if (!AppPreferences.isSendingActive(this)) {
            ExLog.i(TAG, "Sending deactivated. Event will not be sent to server.");
        } else {
            EventSenderTask task = new EventSenderTask(this);
            task.execute(intent);
        }
    }

    @Override
    public void onResult(Intent intent, boolean success, Iterable<RaceLogEvent> eventsToAddToRaceLog) {
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
            ExLog.i(TAG, "Event successfully send.");
            if (persistenceManager.areIntentsDelayed()) {
                persistenceManager.removeIntent(intent);
            }
            lastSuccessfulSend = Calendar.getInstance().getTime();
            serviceLogger.onEventSentSuccessful();
            String raceId = intent.getStringExtra(AppConstants.RACE_ID_KEY);
            RaceLog raceLog = dataManager.getDataStore().getRace(raceId).getRaceLog();
            if (raceLog != null) {
                ExLog.i(TAG, "Successfully retrieved race log for race ID " + raceId);
                for (RaceLogEvent eventToAddToRaceLog : eventsToAddToRaceLog) {
                    raceLog.add(eventToAddToRaceLog);
                    ExLog.i(TAG, "added event "+eventToAddToRaceLog.toString()+" to client's race log");
                }
            } else {
                ExLog.w(TAG, "Couldn't retrieve race log for race ID "+raceId);
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
