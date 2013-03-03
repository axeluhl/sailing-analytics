package com.sap.sailing.racecommittee.app.services;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RaceLogChangedVisitor;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.services.sending.RaceEventSender;
import com.sap.sailing.racecommittee.app.ui.activities.LoginActivity;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorIdJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogEventSerializer;

public class RaceStateService extends Service {
    private final static String TAG = RaceStateService.class.getName();

    /**
     * Binder for this {@link RaceStateService}.
     */
    public class RaceStateServiceBinder extends Binder {
        public RaceStateService getService() {
            return RaceStateService.this;
        }
    }

    private final IBinder mBinder = new RaceStateServiceBinder();

    private UUID serviceId;

    private AlarmManager alarmManager;
    
    private ReadonlyDataManager dataManager;
    
    private Map<ManagedRace, RaceLogEventVisitor> registeredLogListeners;
    private Map<ManagedRace, RaceStateChangedListener> registeredStateListeners;

    @Override
    public void onCreate() {
        this.serviceId = UUID.randomUUID();
        this.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        this.dataManager = DataManager.create(this);
        
        this.registeredLogListeners = new HashMap<ManagedRace, RaceLogEventVisitor>();
        this.registeredStateListeners = new HashMap<ManagedRace, RaceStateChangedListener>();

        startForeground();
        ExLog.i(TAG, "Started.");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            ExLog.i(TAG, "Restarted.");
        } else {
            handleStartCommand(intent);
        }

        // We want this service to continue running until it is explicitly
        // stopped, therefore return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Unregister all race listener...
        for (Entry<ManagedRace, RaceLogEventVisitor> entry : registeredLogListeners.entrySet()) {
            entry.getKey().getState().getRaceLog().removeListener(entry.getValue());
        }
        for (Entry<ManagedRace, RaceStateChangedListener> entry : registeredStateListeners.entrySet()) {
            entry.getKey().getState().unregisterListener(entry.getValue());
        }
        
        // ... and remove from status bar!
        stopForeground(true);
        super.onDestroy();
    }

    private void startForeground() {
        CharSequence text = getText(R.string.service_title);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.sap_sailing_app_icon, text, System.currentTimeMillis());

        Intent intent = new Intent(this, LoginActivity.class);
        // clear all activities above started and reuse if already existing!
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // The PendingIntent to launch our activity if the user selects this
        // notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.service_info), text, contentIntent);
        notification.flags |= Notification.FLAG_NO_CLEAR;

        // Send the notification.
        this.startForeground(42, notification);
    }

    private void handleStartCommand(Intent intent) {
        String action = intent.getAction();
        ExLog.i(TAG, String.format("Command action '%s' received.", action));
        
        if (AppConstants.REGISTER_RACE_ACTION.equals(action)) {
            handleRegisterRace(intent);
            return;
        }
    }

    private void handleRegisterRace(Intent intent) {
        ExLog.i(TAG, "Registering race.");
        ManagedRace race = getRaceFromIntent(intent);
        if (race == null) {
            ExLog.i(TAG, "Intent did not carry valid race information.");
            return;
        }
        registerRace(race);
    }

    private ManagedRace getRaceFromIntent(Intent intent) {
        if (intent.getExtras() == null || !intent.getExtras().containsKey(AppConstants.RACE_ID_KEY)) {
            return null;
        }

        Serializable raceId = intent.getExtras().getSerializable(AppConstants.RACE_ID_KEY);
        return dataManager.getDataStore().getRace(raceId);
    }

    private void registerRace(ManagedRace race) {
        RaceState state = race.getState();
        
        // Register on event additions...
        JsonSerializer<RaceLogEvent> eventSerializer = RaceLogEventSerializer.create(new CompetitorIdJsonSerializer());
        RaceEventSender sender = new RaceEventSender(this, eventSerializer, race);
        RaceLogChangedVisitor logListener = new RaceLogChangedVisitor(sender);
        state.getRaceLog().addListener(logListener);
        
        // ... register on state changes!
        RaceStateChangedListener stateListener = new RaceStateListener(race);
        state.registerListener(stateListener);
        
        this.registeredLogListeners.put(race, logListener);
        this.registeredStateListeners.put(race, stateListener);
    }

}
