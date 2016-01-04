package com.sap.sailing.android.buoy.positioning.app.service;

import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.util.AppPreferences;
import com.sap.sailing.android.buoy.positioning.app.util.CheckinManager;
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.CheckinData;
import com.sap.sailing.android.shared.data.AbstractCheckinData;

public class MarkerService extends Service implements  CheckinManager.DataChangedListner{
    private static String TAG = MarkerService.class.getName();
    private MarkerBinder markerBinder;
    private CheckinManager manager;
    private TimerRunnable timerRunnable;
    private AppPreferences preferences;
    private long lastCheck;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MarkService created");
        markerBinder = new MarkerBinder();
        preferences = new AppPreferences(this);
        lastCheck = Calendar.getInstance().getTimeInMillis();
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "MarkService is bound");
        return markerBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String url = intent.getStringExtra(getString(R.string.check_in_url_key));
        manager = new CheckinManager(url, this);
        manager.setDataChangedListner(this);
        timerRunnable = new TimerRunnable();
        new Thread(timerRunnable).start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        timerRunnable.stop();
        super.onDestroy();
    }

    @Override
    public void handleData(AbstractCheckinData data) {
        try {
            CheckinData checkinData = (CheckinData) data;
            DatabaseHelper helper = DatabaseHelper.getInstance();
            helper.deleteRegattaFromDatabase(this, checkinData.checkinDigest);
            helper.storeCheckinRow(this, checkinData.marks, checkinData.getLeaderboard(), checkinData.getCheckinUrl(), checkinData.pings);
        }
        catch (DatabaseHelper.GeneralDatabaseHelperException e) {
            e.printStackTrace();
        }
    }

    private class MarkerBinder extends Binder {
        @SuppressWarnings("unused")
        // Unused for now. Will be useful in case this service becomes a bound service.
        public MarkerService getService() {
            return MarkerService.this;
        }
    }

    private class TimerRunnable implements Runnable {
        ;
        private  boolean running;

        public TimerRunnable(){
            running = true;
        }

        @Override
        public void run() {
            while (running) {
                long timeDiff = Calendar.getInstance().getTimeInMillis() - lastCheck;

                if(timeDiff > preferences.getDataRefreshInterval() * 1000) {
                    manager.callServerAndGenerateCheckinData();
                    lastCheck = Calendar.getInstance().getTimeInMillis();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stop() {
            running = false;
        }
    }

}
