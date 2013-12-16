package com.sap.sailing.domain.igtimiadapter.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.LiveDataConnection;
import com.sap.sailing.domain.igtimiadapter.Permission;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.tracking.AbstractWindTracker;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.WindTracker;

public class IgtimiWindTracker extends AbstractWindTracker implements WindTracker {
    private static final Logger logger = Logger.getLogger(IgtimiWindTracker.class.getName());
    private static final int TIME_INTERVAL_TO_TRACK_BEFORE_RACE_START_MILLIS = 10*60*1000; // 10 minutes
    private static final long TIME_INTERVAL_TO_TRACK_AFTER_END_OF_RACE_MILLIS = 60*60*1000; // 60 minutes
    private final Map<LiveDataConnection, Pair<Set<String>, Account>> liveConnectionsAndDeviceSerialNumber;
    private final IgtimiWindTrackerFactory windTrackerFactory;
    private boolean stopping;

    protected IgtimiWindTracker(final DynamicTrackedRace trackedRace, final IgtimiConnectionFactory connectionFactory,
            final IgtimiWindTrackerFactory windTrackerFactory) throws Exception {
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
                                // find all the devices from which we may read
                                Iterable<DataAccessWindow> dataAccessWindows = connection.getDataAccessWindows(
                                        Permission.read, /* start time */ null, /* end time */ null,
                                        /* get data for all available deviceSerialNumbers */null);
                                Set<String> deviceSerialNumbersWeCanRead = new HashSet<>();
                                for (DataAccessWindow daw : dataAccessWindows) {
                                    deviceSerialNumbersWeCanRead.add(daw.getDeviceSerialNumber());
                                }
                                // find all that haven't even sent GPS; those may never have sent ever, so we need to listen to them for new stuff; they could be wind sensors
                                Iterable<Fix> gpsFixes = connection.getLatestFixes(deviceSerialNumbersWeCanRead, Type.gps_latlong);
                                Set<String> devicesWithGps = getDeviceSerialNumbers(gpsFixes);
                                Iterable<Fix> awsFixes = connection.getLatestFixes(deviceSerialNumbersWeCanRead, Type.AWS); // look for latest fixes with apparent wind speed in the fix
                                Set<String> devicesWithWind = getDeviceSerialNumbers(awsFixes);
                                Set<String> devicesThatHaveNeverSentGpsNorWind = new HashSet<>(deviceSerialNumbersWeCanRead);
                                devicesThatHaveNeverSentGpsNorWind.removeAll(devicesWithGps);
                                devicesThatHaveNeverSentGpsNorWind.removeAll(devicesWithWind);
                                Set<String> devicesWeShouldListenTo = new HashSet<>();
                                devicesWeShouldListenTo.addAll(devicesWithWind);
                                devicesWeShouldListenTo.addAll(devicesThatHaveNeverSentGpsNorWind);
                                logger.info("Will listen to devices "
                                        + devicesWeShouldListenTo
                                        + " because from all devices "+deviceSerialNumbersWeCanRead+" for "
                                        + devicesThatHaveNeverSentGpsNorWind
                                        + " we don't know what they are as they never sent anything we can access, and for "
                                        + devicesWithWind + " we know they sent wind");
                                if (!stopping) {
                                    LiveDataConnection liveConnection = connection.createLiveConnection(devicesWeShouldListenTo);
                                    IgtimiWindReceiver windReceiver = new IgtimiWindReceiver(devicesWeShouldListenTo);
                                    liveConnection.addListener(windReceiver);
                                    windReceiver.addListener(new WindListenerSendingToTrackedRace(Collections.singleton(getTrackedRace()), windTrackerFactory));
                                    liveConnectionsAndDeviceSerialNumber.put(liveConnection, new Pair<Set<String>, Account>(devicesWeShouldListenTo, account));
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

    private Set<String> getDeviceSerialNumbers(Iterable<Fix> fixes) {
        Set<String> deviceSerialNumbers = new HashSet<>();
        for (Fix fix : fixes) {
            deviceSerialNumbers.add(fix.getSensor().getDeviceSerialNumber());
        }
        return deviceSerialNumbers;
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
                    final Pair<Set<String>, Account> deviceSerialNumberAndAccount = liveConnectionsAndDeviceSerialNumber.get(ldc);
                    logger.log(Level.INFO,
                            "Exception trying to stop Igtimi live connection for wind receiver for race "
                                    + getTrackedRace().getRace() + " and device " + deviceSerialNumberAndAccount.getA()
                                    + " in account " + deviceSerialNumberAndAccount.getB());
                }
            }
        }
    }
}
