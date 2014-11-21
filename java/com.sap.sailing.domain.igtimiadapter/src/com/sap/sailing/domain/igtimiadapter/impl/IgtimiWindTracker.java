package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.declination.DeclinationService;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.LiveDataConnection;
import com.sap.sailing.domain.igtimiadapter.shared.IgtimiWindReceiver;
import com.sap.sailing.domain.tracking.AbstractWindTracker;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class IgtimiWindTracker extends AbstractWindTracker implements WindTracker {
    private static final Logger logger = Logger.getLogger(IgtimiWindTracker.class.getName());
    private static final int TIME_INTERVAL_TO_TRACK_BEFORE_RACE_START_MILLIS = 10*60*1000; // 10 minutes
    private static final long TIME_INTERVAL_TO_TRACK_AFTER_END_OF_RACE_MILLIS = 60*60*1000; // 60 minutes
    private final Map<LiveDataConnection, Util.Pair<Iterable<String>, Account>> liveConnectionsAndDeviceSerialNumber;
    private final IgtimiWindTrackerFactory windTrackerFactory;
    private boolean stopping;

    protected IgtimiWindTracker(final DynamicTrackedRace trackedRace, final IgtimiConnectionFactory connectionFactory,
            final IgtimiWindTrackerFactory windTrackerFactory, final boolean correctByDeclination) throws Exception {
        super(trackedRace);
        this.windTrackerFactory = windTrackerFactory;
        liveConnectionsAndDeviceSerialNumber = new HashMap<>();
        new Thread("IgtimiWindTracker start-up thread for race " + trackedRace.getRace().getName()) {
            public void run() {
                logger.info("Starting up Igtimi wind tracker for race "+trackedRace.getRace().getName());
                // avoid a race condition with stop() being called while this start-up thread is still running
                synchronized (IgtimiWindTracker.this) {
                    Iterable<Account> accounts = connectionFactory.getAllAccounts();
                    for (Account account : accounts) {
                        try {
                            if (!stopping) {
                                IgtimiConnection connection = connectionFactory.connect(account);
                                Iterable<String> devicesWeShouldListenTo = connection.getWindDevices();
                                if (!stopping) {
                                    LiveDataConnection liveConnection = connection.getOrCreateLiveConnection(devicesWeShouldListenTo);
                                    IgtimiWindReceiver windReceiver = new IgtimiWindReceiver(correctByDeclination ? DeclinationService.INSTANCE : null);
                                    liveConnection.addListener(windReceiver);
                                    windReceiver.addListener(new WindListenerSendingToTrackedRace(Collections.singleton(getTrackedRace()), windTrackerFactory));
                                    liveConnectionsAndDeviceSerialNumber.put(liveConnection, new Util.Pair<Iterable<String>, Account>(devicesWeShouldListenTo, account));
                                }
                            }
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Exception trying to start Igtimi wind tracker for race "
                                    + getTrackedRace().getRace().getName() + " for account " + account);
                        }
                    }
                }
            }
        }.start();
    }

    public static TimePoint getReceivingEndTime(DynamicTrackedRace trackedRace) {
        TimePoint endOfRace = trackedRace.getEndOfRace();
        TimePoint endOfTracking = trackedRace.getEndOfTracking();
        final TimePoint endTime;
        if (endOfRace == null) {
            if (endOfTracking == null) {
                endTime = null;
            } else {
                endTime = endOfTracking.plus(TIME_INTERVAL_TO_TRACK_AFTER_END_OF_RACE_MILLIS);
            }
        } else {
            if (endOfTracking == null) {
                endTime = endOfRace;
            } else {
                endTime = Collections.max(Arrays.asList(endOfRace, endOfTracking));
            }
        }
        return endTime;
    }

    /**
     * Based on the tracked race's time parameters and the current time, decides for a good start time for receiving
     * live wind data. Get all devices that have been sending during the last 10 minutes before start of race, start of
     * tracking or now, whichever came first.
     */
    public static TimePoint getReceivingStartTime(DynamicTrackedRace trackedRace) {
        List<TimePoint> startCandidates = new ArrayList<>();
        final TimePoint startOfRace = trackedRace.getStartOfRace();
        if (startOfRace != null) {
            startCandidates.add(startOfRace);
        }
        final TimePoint startOfTracking = trackedRace.getStartOfTracking();
        if (startOfTracking != null) {
            startCandidates.add(startOfTracking);
        }
        startCandidates.add(MillisecondsTimePoint.now());
        return Collections.min(startCandidates).minus(TIME_INTERVAL_TO_TRACK_BEFORE_RACE_START_MILLIS);
    }

    @Override
    public void stop() {
        logger.info("Stopping Igtimi wind tracker for race "+getTrackedRace().getRace().getName());
        stopping = true; // in case stop() is called while the start-up thread is still running, avoid more connections to be created
        // this synchronizes with the start-up thread and waits until that has finished and no longer writes to liveConnectionsAndDeviceSerialNumber
        synchronized (this) {
            windTrackerFactory.windTrackerStopped(getTrackedRace().getRace(), this);
            for (LiveDataConnection ldc : liveConnectionsAndDeviceSerialNumber.keySet()) {
                try {
                    logger.info("Stopping Igtimi live connection "+ldc);
                    ldc.stop();
                } catch (Exception e) {
                    final Util.Pair<Iterable<String>, Account> deviceSerialNumberAndAccount = liveConnectionsAndDeviceSerialNumber.get(ldc);
                    logger.log(Level.INFO,
                            "Exception trying to stop Igtimi live connection for wind receiver for race "
                                    + getTrackedRace().getRace() + " and device " + deviceSerialNumberAndAccount.getA()
                                    + " in account " + deviceSerialNumberAndAccount.getB());
                }
            }
        }
    }
}
