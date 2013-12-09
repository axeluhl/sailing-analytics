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
    private final Map<LiveDataConnection, Pair<String, Account>> liveConnectionsAndDeviceSerialNumber;

    protected IgtimiWindTracker(DynamicTrackedRace trackedRace, IgtimiConnectionFactory connectionFactory) throws Exception {
        super(trackedRace);
        liveConnectionsAndDeviceSerialNumber = new HashMap<>();
        Iterable<Account> accounts = connectionFactory.getAllAccounts();
        for (Account account : accounts) {
            IgtimiConnection connection = connectionFactory.connect(account);
            Iterable<DataAccessWindow> dataAccessWindows = connection.getDataAccessWindows(Permission.read, getReceivingStartTime(trackedRace),
                    /* endTime */null, /* get data for all available deviceSerialNumbers */null);
            for (DataAccessWindow daw : dataAccessWindows) {
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
                    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
                    }
                });
                liveConnectionsAndDeviceSerialNumber.put(liveConnection, new Pair<String, Account>(deviceSerialNumber, account));
            }
        }
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
        return Collections.min(Arrays.asList(new TimePoint[] { trackedRace.getStartOfRace(), trackedRace.getStartOfTracking(), MillisecondsTimePoint.now() })).minus(10*60*1000);
    }

    @Override
    public void stop() {
        for (LiveDataConnection ldc : liveConnectionsAndDeviceSerialNumber.keySet()) {
            try {
                ldc.stop();
            } catch (Exception e) {
                final Pair<String, Account> deviceSerialNumberAndAccount = liveConnectionsAndDeviceSerialNumber.get(ldc);
                logger.log(Level.INFO, "Exception trying to stop Igtimi live connection for wind receiver for race "
                        + getTrackedRace().getRace() + " and device " + deviceSerialNumberAndAccount.getA()
                        + " in account " + deviceSerialNumberAndAccount.getB());
            }
        }
    }
}
