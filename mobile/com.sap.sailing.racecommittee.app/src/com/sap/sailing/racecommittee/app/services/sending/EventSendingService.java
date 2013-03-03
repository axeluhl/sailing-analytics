package com.sap.sailing.racecommittee.app.services.sending;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;
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
    private boolean isHandlerSet;

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

    public int getDelayedIntentsCount() {
        return persistenceManager.getEventCount();
    }

    public Date getLastSuccessfulSend() {
        return lastSuccessfulSend;
    }

    /**
     * creates an intent that contains the event to be sent and the race id which shall be sent to the backend
     * 
     * @param context
     *            the context of the app
     * @param race
     *            the race for which the event was created
     * @param serializedEvent
     *            the event serizalized to JSON
     * @return the intent that shall be sent to the EventSendingService
     */
    public static Intent createEventIntent(Context context, ManagedRace race, Serializable serializedEvent) {
        String url = String.format(
                "%s/sailingserver/rc/racelog?leaderboard=%s&raceColumn=%s&fleet=%s", 
                AppConstants.getServerBaseURL(context),
                URLEncoder.encode(race.getRaceGroup().getName()),
                URLEncoder.encode(race.getName()),
                URLEncoder.encode(race.getFleet().getName()));
        
        Intent eventIntent = new Intent(context.getString(R.string.intentActionSendEvent));
        eventIntent.putExtra(AppConstants.RACE_ID_KEY, race.getId());
        eventIntent.putExtra(AppConstants.EXTRAS_JSON_KEY, serializedEvent);
        eventIntent.putExtra(AppConstants.EXTRAS_URL, url);
        ExLog.i(TAG, "Created event " + eventIntent + " for sending to backend");
        return eventIntent;
    }
    
    public static Intent createEventIntent(Context context, Serializable raceId, Serializable serializedEvent) {
        ReadonlyDataManager data = DataManager.create(context);
        ManagedRace race = data.getDataStore().getRace(raceId);
        if (race == null) {
            ExLog.e(TAG, "There is no race with id " + raceId);
            return null;
        }
        return createEventIntent(context, race, serializedEvent);
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
        if (action.equals(getString(R.string.intentActionSendSavedIntents))) {
            handleDelayedEvents();
        } else if (action.equals(getString(R.string.intentActionSendEvent))) {
            handleSendEvents(intent);
        }
    }

    private void handleSendEvents(Intent intent) {
        if (!isConnected()) {
            persistenceManager.persistIntent(intent);
            //ConnectivityChangedReceiver.enable(this);
        } else {
            sendEvent(intent);
        }
    }

    private void handleDelayedEvents() {
        isHandlerSet = false;
        if (!isConnected())
            return;

        sendDelayedEvents();
    }

    private void sendDelayedEvents() {
        List<Intent> delayedIntents = persistenceManager.restoreEvents();
        ExLog.i(TAG, String.format("Trying to resend %d waiting events", delayedIntents.size()));
        for (Intent intent : delayedIntents)
            sendEvent(intent);
    }

    private void sendEvent(Intent intent) {
        if (!AppConstants.isSendingActive(this)) {
            ExLog.i(TAG, "Sending deactivated. Event will not be sent to server.");
        } else {
            EventSenderTask task = new EventSenderTask(this);
            task.execute(intent);
        }
    }

    public void onResult(Intent intent, boolean success) {
        if (!success) {
            ExLog.w(TAG, "Could not POST intent to server.");
            persistenceManager.persistIntent(intent);
            if (!isHandlerSet) {
                SendDelayedEventsCaller delayedCaller = new SendDelayedEventsCaller(this);
                handler.postDelayed(delayedCaller, 1000 * 60 * 1);
                isHandlerSet = true;
            }
        } else {
            ExLog.i(TAG, "Event successfully send. " + intent.getStringExtra(AppConstants.EXTRAS_JSON_KEY));
            persistenceManager.removeIntent(intent);
            lastSuccessfulSend = Calendar.getInstance().getTime();
        }

    }

    /**
     * checks if there is network connectivity
     * 
     * @return connectivity check value
     */
    private boolean isConnected() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork == null)
            return false;
        boolean isConnected = activeNetwork.isConnected();
        return isConnected;
    }
}
