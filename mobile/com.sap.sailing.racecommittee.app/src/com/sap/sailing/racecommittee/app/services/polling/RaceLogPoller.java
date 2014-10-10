package com.sap.sailing.racecommittee.app.services.polling;

import java.io.InputStream;
import java.io.Serializable;
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
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RaceLogEventsCallback;
import com.sap.sailing.racecommittee.app.services.polling.RaceLogPollerTask.PollingResultListener;
import com.sap.sse.common.Util;

/**
 * <p>
 * Polls for server-side race log changes
 * </p>
 * 
 * <p>
 * There is no multi-threading involved, everything will be done on the UI thread.
 * </p>
 */
public class RaceLogPoller implements PollingActiveChangedListener {

    protected static final String TAG = RaceLogPoller.class.getName();

    private final Context context;
    private final Handler pollingHandler;
    private final PollingWorker pollingWorker;
    private final Map<ManagedRace, URL> races;
    private final AppPreferences appPreferences;
    private boolean hasRacesToPoll;

    public RaceLogPoller(Context context) {
        this.context = context;
        // We want to use the main (UI) loop
        this.pollingHandler = new Handler(Looper.getMainLooper());
        this.pollingWorker = new PollingWorker(this, context);
        this.races = new HashMap<ManagedRace, URL>();
        this.appPreferences = AppPreferences.on(context);
        this.appPreferences.registerPollingActiveChangedListener(this);
        this.hasRacesToPoll = false;
    }

    public void register(ManagedRace race) {
        try {
            races.put(race, createURL(race));
            // remove pending pollingWorker, to ensure that we only have one at once
            pollingHandler.removeCallbacks(pollingWorker);
            long pollingInterval = getPollingIntervalInMs();
            pollingHandler.postDelayed(pollingWorker, pollingInterval);
            this.hasRacesToPoll = true;
            ExLog.i(context, TAG, String.format("Registered race %s for polling, will start in %d milliseconds.", race.getId(),
                    pollingInterval));
        } catch (MalformedURLException e) {
            ExLog.e(context, TAG, String.format("Unable to create polling URL for race %s: %s", race.getId(), e.getMessage()));
        }
    }

    private URL createURL(ManagedRace race) throws MalformedURLException {
        return new URL(MessageSendingService.getRaceLogEventSendAndReceiveUrl(context, race.getRaceGroup().getName(),
                race.getName(), race.getFleet().getName()));
    }

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
    };

    /**
     * Will be run on the main (UI) thread!
     */
    private static class PollingWorker implements Runnable, PollingResultListener {
        
        private final RaceLogPoller poller;
        private final RaceLogEventsCallback processor;
        private RaceLogPollerTask task;
        private Context context;
        
        public PollingWorker(RaceLogPoller poller, Context context) {
            this.poller = poller;
            this.processor = new RaceLogEventsCallback();
            this.context = context;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            ExLog.i(context, TAG, "Polling for server-side race log changes...");
            if (!poller.isPollingActive()) {
                ExLog.i(context, TAG, "Polling aborted.");
                return;
            }
            
            List<Util.Pair<Serializable, URL>> queries = getPollingQueries();
            task = new RaceLogPollerTask(this, context);
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
            List<Util.Pair<Serializable, URL>> queries = new ArrayList<Util.Pair<Serializable,URL>>();
            for (Entry<ManagedRace, URL> entry : poller.races.entrySet()) {
                queries.add(new Util.Pair<Serializable, URL>(entry.getKey().getId(), entry.getValue()));
            }
            return queries;
        }
    }

}
