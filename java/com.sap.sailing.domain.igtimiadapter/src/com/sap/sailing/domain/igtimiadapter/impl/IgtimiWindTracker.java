package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.LiveDataConnection;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.tracking.AbstractWindTracker;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.domain.tracking.WindTracker;

public class IgtimiWindTracker extends AbstractWindTracker implements WindTracker {
    private static final Logger logger = Logger.getLogger(IgtimiWindTracker.class.getName());
    private static final int TIME_INTERVAL_TO_TRACK_BEFORE_RACE_START_MILLIS = 10*60*1000; // 10 minutes
    private static final long TIME_INTERVAL_TO_TRACK_AFTER_END_OF_RACE_MILLIS = 60*60*1000; // 60 minutes
    private final Map<LiveDataConnection, Pair<String, Account>> liveConnectionsAndDeviceSerialNumber;
    private final IgtimiWindTrackerFactory windTrackerFactory;
    private boolean stopping;

    protected IgtimiWindTracker(final DynamicTrackedRace trackedRace, final IgtimiConnectionFactory connectionFactory,
            IgtimiWindTrackerFactory windTrackerFactory) throws Exception {
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
                                final TimePoint endTime = getReceivingEndTime(trackedRace);
                                Iterable<DataAccessWindow> dataAccessWindows = connection.getDataAccessWindows(
                                        Permission.read, getReceivingStartTime(trackedRace), endTime,
                                        /* get data for all available deviceSerialNumbers */null);
                                for (DataAccessWindow daw : dataAccessWindows) {
                                    if (!stopping) {
                                        final String deviceSerialNumber = daw.getDeviceSerialNumber();
                                        final WindSource windSource = getWindSource(deviceSerialNumber);
                                        LiveDataConnection liveConnection = connection.createLiveConnection(Collections.singleton(deviceSerialNumber));
                                        IgtimiWindReceiver windReceiver = new IgtimiWindReceiver(Collections.singleton(deviceSerialNumber));
                                        liveConnection.addListener(windReceiver);
                                        windReceiver.addListener(new WindListener() {
                                            @Override
                                            public void windDataReceived(Wind wind) {
                                                getTrackedRace().recordWind(wind, windSource);
                                            }

                                            @Override
                                            public void windDataRemoved(Wind wind) {
                                            }

                                            @Override
                                            public void windAveragingChanged(long oldMillisecondsOverWhichToAverage,
                                                    long newMillisecondsOverWhichToAverage) {
                                            }
                                        });
                                        liveConnectionsAndDeviceSerialNumber.put(liveConnection,
                                                new Pair<String, Account>(deviceSerialNumber, account));
                                    }
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

    private TimePoint getReceivingEndTime(DynamicTrackedRace trackedRace) {
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

    private WindSource getWindSource(String deviceSerialNumber) {
        return new WindSourceWithAdditionalID(WindSourceType.EXPEDITION, deviceSerialNumber);
    }

    /**
     * Based on the tracked race's time parameters and the current time, decides for a good start time for receiving
     * live wind data. Get all devices that have been sending during the last 10 minutes before start of race, start of
     * tracking or now, whichever came first.
     */
    private TimePoint getReceivingStartTime(DynamicTrackedRace trackedRace) {
        return Collections.min(Arrays.asList(new TimePoint[] { trackedRace.getStartOfRace(), trackedRace.getStartOfTracking(), MillisecondsTimePoint.now() })).
                minus(TIME_INTERVAL_TO_TRACK_BEFORE_RACE_START_MILLIS);
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
                    final Pair<String, Account> deviceSerialNumberAndAccount = liveConnectionsAndDeviceSerialNumber
                            .get(ldc);
                    logger.log(Level.INFO,
                            "Exception trying to stop Igtimi live connection for wind receiver for race "
                                    + getTrackedRace().getRace() + " and device " + deviceSerialNumberAndAccount.getA()
                                    + " in account " + deviceSerialNumberAndAccount.getB());
                }
            }
        }
    }
}
