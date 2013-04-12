package com.sap.sailing.racecommittee.app.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
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
import com.sap.sailing.server.gateway.serialization.racegroup.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;

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
    
    private final static String EXTRAS_SERVICE_ID = RaceStateService.class.getName() + ".serviceId";

    private final IBinder mBinder = new RaceStateServiceBinder();

    private UUID serviceId;

    private AlarmManager alarmManager;
    
    private int alarmManagerRequestCode = 0;
    
    private ReadonlyDataManager dataManager;
    
    private Map<ManagedRace, RaceLogEventVisitor> registeredLogListeners;
    private Map<ManagedRace, RaceStateChangedListener> registeredStateListeners;
    
    private Map<Serializable, List<PendingIntent>> managedIntents;

    @Override
    public void onCreate() {
        this.serviceId = UUID.randomUUID();
        this.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        this.dataManager = DataManager.create(this);
        
        this.registeredLogListeners = new HashMap<ManagedRace, RaceLogEventVisitor>();
        this.registeredStateListeners = new HashMap<ManagedRace, RaceStateChangedListener>();
        this.managedIntents = new HashMap<Serializable, List<PendingIntent>>();

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
        
        for (List<PendingIntent> intents : managedIntents.values()) {
            for (PendingIntent intent : intents) {
                alarmManager.cancel(intent);
            }
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
        
        if (getString(R.string.intentActionRegisterRace).equals(action)) {
            handleRegisterRace(intent);
            return;
        }
        
        if (!serviceId.equals(intent.getSerializableExtra(EXTRAS_SERVICE_ID))) {
            ExLog.w(TAG, "Received event for different service version.");
            return;
        }
        
        Serializable id = intent.getSerializableExtra(AppConstants.RACE_ID_KEY);
        ManagedRace race = dataManager.getDataStore().getRace(id);
        if (race == null) {
            ExLog.w(TAG, "No race for id " + id);
            return;
        }
        
        TimePoint eventTime = new MillisecondsTimePoint(intent.getExtras().getLong(AppConstants.RACING_EVENT_TIME));
        
        if (getString(R.string.intentActionAlarmAction).equals(action)) {
            if (race.getState().getStartTime() != null) {
                race.getState().getStartProcedure().dispatchFiredEventTimePoint(race.getState().getStartTime(), eventTime);
            }
            managedIntents.get(race.getId()).remove(intent);
            return;
        } else if (getString(R.string.intentActionIndividualRecallRemoval).equals(action)) {
            race.getState().getStartProcedure().dispatchFiredIndividualRecallRemovalEvent(race.getState().getIndividualRecallDisplayedTime(), eventTime);
        } else if (getString(R.string.intentActionAutomaticRaceEnd).equals(action)) {
            race.getState().getStartProcedure().dispatchAutomaticRaceEndEvent(eventTime);
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
        ExLog.i(TAG, "Trying to register race " + race.getId());
        
        if (!managedIntents.containsKey(race.getId())) {
            RaceState state = race.getState();
            managedIntents.put(race.getId(), new ArrayList<PendingIntent>());
            registerAlarms(race, state);

            // Register on event additions...
            JsonSerializer<RaceLogEvent> eventSerializer = RaceLogEventSerializer.create(new CompetitorJsonSerializer());
            RaceEventSender sender = new RaceEventSender(this, eventSerializer, race);
            RaceLogChangedVisitor logListener = new RaceLogChangedVisitor(sender);
            state.getRaceLog().addListener(logListener);

            // ... register on state changes!
            RaceStateChangedListener stateListener = new RaceStateListener(this, race);
            state.registerListener(stateListener);

            this.registeredLogListeners.put(race, logListener);
            this.registeredStateListeners.put(race, stateListener);
        } else {
            ExLog.i(TAG, "Race " + race.getId() + " was already registered. Ignoring.");
        }
    }

    public void registerAlarms(ManagedRace race, RaceState state) {
        switch (state.getStatus()) {
        case SCHEDULED:
        case STARTPHASE:
            handleNewStartTime(race, state.getStartTime());
            break;
        case RUNNING:
            //TODO check for individual recall removal event
        default:
            break;
        }
    }

    public void handleNewStartTime(ManagedRace race, TimePoint startTime) {
        clearAlarms(race.getId());
        
        //formerly state.getStartTime().plus(1) don't know why
        
        List<TimePoint> fireTimePoints = race.getState().getStartProcedure().getAutomaticEventFireTimePoints(startTime);
        String action = getString(R.string.intentActionAlarmAction);
        for (TimePoint eventFireTimePoint : fireTimePoints) {
            scheduleEventTime(action, race, eventFireTimePoint);
        }
        ExLog.i(TAG, "Race " + race.getId() + " is scheduled now.");
    }

    private void scheduleEventTime(String action, ManagedRace race, TimePoint eventFireTimePoint) {
        addIntentToAlarmManager(action, race, eventFireTimePoint);
    }

    private void addIntentToAlarmManager(String action, ManagedRace managedRace, TimePoint eventFireTimePoint) {
        PendingIntent pendingIntent = createPendingIntent(action, managedRace, eventFireTimePoint);
        managedIntents.get(managedRace.getId()).add(pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, eventFireTimePoint.asMillis(), pendingIntent);
        ExLog.i(TAG, "The alarm " + action + " will be fired at " + eventFireTimePoint);
    }

    private PendingIntent createPendingIntent(String action, ManagedRace managedRace, TimePoint eventFireTimePoint) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRAS_SERVICE_ID, serviceId);
        intent.putExtra(AppConstants.RACE_ID_KEY, managedRace.getId());
        intent.putExtra(AppConstants.RACING_EVENT_TIME, eventFireTimePoint.asMillis());
        PendingIntent pendingIntent = PendingIntent.getService(this, alarmManagerRequestCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
    
    /**
     * removes already set alarm times for a given managed race
     * 
     * @param managedRace
     *            the race whose alarms shall be removed from alarmManager
     */
    public void clearAlarms(Serializable raceId) {
        List<PendingIntent> intents = managedIntents.get(raceId);
        
        if (intents == null) {
            ExLog.w(TAG, "There are no intents for race " + raceId);
            return;
        }
        
        for (PendingIntent pendingIntent : intents) {
            alarmManager.cancel(pendingIntent);
        }
        
        intents.clear();
    }

    public void handleRaceAborted(ManagedRace race) {
        clearAlarms(race.getId());
    }

    public void handleIndividualRecall(ManagedRace race, TimePoint individualRecallRemovalFireTimePoint) {
        String action = getString(R.string.intentActionIndividualRecallRemoval);
        scheduleEventTime(action, race, individualRecallRemovalFireTimePoint);
    }

    public void handleIndividualRecallRemoved(ManagedRace race) {
        clearAlarms(race.getId());
    }

    public void handleAutomaticRaceEnd(ManagedRace race, TimePoint automaticRaceEnd) {
        String action = getString(R.string.intentActionAutomaticRaceEnd);
        scheduleEventTime(action, race, automaticRaceEnd);
    }

}
