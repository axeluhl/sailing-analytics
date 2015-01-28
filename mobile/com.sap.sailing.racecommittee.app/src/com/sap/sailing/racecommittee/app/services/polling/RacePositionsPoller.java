package com.sap.sailing.racecommittee.app.services.polling;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.AppPreferences.PollingActiveChangedListener;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RacePositionsCallback;
import com.sap.sailing.racecommittee.app.ui.utils.OnRaceUpdatedListener;
import com.sap.sse.common.Util;

/**
 * <p>
 * Polls for server-side race buoy and boat position changes
 * </p>
 * 
 * <p>
 * There is no multi-threading involved, everything will be done on the UI thread. Thanks Obama.
 * </p>
 */
public class RacePositionsPoller implements PollingActiveChangedListener {

    protected static final String TAG = RacePositionsPoller.class.getName();

    private final Context context;
    private final Handler pollingHandler;
    private final PollingWorker pollingWorker;
    private final Map<ManagedRace, URL> races;
    private final AppPreferences appPreferences;
    private boolean hasRacesToPoll;

    public RacePositionsPoller(Context context) {
        this.context = context;
        // We want to use the main (UI) loop
        this.pollingHandler = new Handler(Looper.getMainLooper());
        this.pollingWorker = new PollingWorker(this, context);
        this.races = new HashMap<>();
        this.appPreferences = AppPreferences.on(context);
        this.appPreferences.registerPollingActiveChangedListener(this);
        this.hasRacesToPoll = false;
    }

    /**
     * registers a race to the poller and starts immediate polling
     * @param race the race to register
     */
    public void register(ManagedRace race) {
        try {
            races.put(race, createURL(race));
            // remove pending pollingWorker, to ensure that we only have one at once
            pollingHandler.removeCallbacks(pollingWorker);
            // get initial data
            pollingHandler.post(pollingWorker);
            // and setup updates
            //pollingHandler.postDelayed(pollingWorker, pollingInterval);
            this.hasRacesToPoll = true;
            ExLog.i(context, TAG, String.format("Registered race %s for position polling, will start now.", race.getId()));
        } catch (MalformedURLException e) {
            ExLog.e(context, TAG, String.format("Unable to create polling URL for race %s: %s", race.getId(), e.getMessage()));
        } catch (UnsupportedEncodingException e) {
            ExLog.ex(context, TAG, e);
        }
    }

    /**
     * registers a race to the poller and starts immediate polling
     * @param race the race to register
     * @param listener callback that gets handed back the race data once it is available
     */
    public void register(ManagedRace race, OnRaceUpdatedListener listener){
        register(race);
        this.pollingWorker.register(listener);
    }

    /**
     * creates the url needed to poll the server
     * @param race the race that needs a polling url
     * @return URL to poll
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    private URL createURL(ManagedRace race) throws MalformedURLException, UnsupportedEncodingException {
        URL u = new URL(MessageSendingService.getRacePositionsUrl(context, race.getRaceGroup().getName(),
                race.getName()));

        // TODO: remove this debug line:
        u = new URL("http://ec2-54-171-89-140.eu-west-1.compute.amazonaws.com:8888/sailingserver/api/v1/regattas/ESS%202014%20Singapore%20%28Extreme40%29/races/Race%201/marks/positions");

        ExLog.i(context, TAG, String.format("Using url %s", u));
        return u;
    }

    /**
     * stops the polling for updates and clears the races
     */
    public void unregisterAllAndStop() {
        hasRacesToPoll = false;
        pollingHandler.removeCallbacksAndMessages(null);
        races.clear();
        appPreferences.unregisterPollingActiveChangedListener(this);
        ExLog.i(context, TAG, "Polling will be stopped.");
    }

    protected boolean isPollingActive() {
        return hasRacesToPoll && appPreferences.isPollingActive();
    }

    protected long getPollingIntervalInMs() {
        return appPreferences.getPollingInterval() * 1000;
    }

    @Override
    public void onPollingActiveChanged(boolean isActive) {
        if (isActive) {
            long pollingInterval = getPollingIntervalInMs();
            pollingHandler.postDelayed(pollingWorker, pollingInterval);
            ExLog.i(context, TAG, String.format("Polling has been activated, will start in %d milliseconds.", pollingInterval));
        } else {
            ExLog.i(context, TAG, "Polling has been deactivated, next polling attempt will be aborted.");
        }
    }

    /**
     * Will be run on the main (UI) thread!
     */
    private static class PollingWorker implements Runnable, RacePositionsPollerTask.PollingResultListener {

        private final RacePositionsPoller poller;
        private final RacePositionsCallback processor;
        private RacePositionsPollerTask task;
        private Context context;

        public PollingWorker(RacePositionsPoller poller, Context context) {
            this.poller = poller;
            this.processor = new RacePositionsCallback();
            this.context = context;
        }

        /**
         * registers a callback
         * @param listener the object containing the callback function
         */
        public void register( OnRaceUpdatedListener listener ){
            this.processor.register(listener);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            ExLog.i(context, TAG, "Polling for server-side race position changes...");
            if (!poller.isPollingActive()) {
                ExLog.i(context, TAG, "Polling aborted.");
                return;
            }
            
            List<Util.Pair<Serializable, URL>> queries = getPollingQueries();
            task = new RacePositionsPollerTask(this, context);
            task.execute(queries.toArray(new Util.Pair[0]));
        }

        @Override
        public void onPollingResult(PollingResult result) {
            if (!poller.isPollingActive()) {
                task.cancel(true);
                ExLog.i(context, TAG, "Polling aborted.");
                return;
            }
            if (result.isSuccess) {
                Serializable raceId = result.resultStreamForRaceId.getA();
                InputStream responseStream = result.resultStreamForRaceId.getB();
                processor.processResponse(poller.context, responseStream, raceId.toString());
            } else {
                ExLog.i(context, TAG, "Polling attempt not successful.");
            }
        }
        
        @Override
        public void onPollingFinished() {
            if (!poller.isPollingActive()) {
                ExLog.i(context, TAG, "Polling aborted.");
                return;
            }
            long pollingInterval = poller.getPollingIntervalInMs();
            ExLog.i(context, TAG, String.format("Polling done. Will poll again in %d milliseconds.", pollingInterval));
            poller.pollingHandler.postDelayed(this, pollingInterval);
        }

        private List<Util.Pair<Serializable, URL>> getPollingQueries() {
            List<Util.Pair<Serializable, URL>> queries = new ArrayList<>();
            for (Entry<ManagedRace, URL> entry : poller.races.entrySet()) {
                queries.add(new Util.Pair<>(entry.getKey().getId(), entry.getValue()));
            }
            return queries;
        }
    }

}
