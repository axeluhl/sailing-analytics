package com.sap.sailing.domain.swisstimingadapter.impl;

import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;

/**
 * Delivers SwissTiming messages with a constant delay (default 250ms) between them. This produces a simulated "live feed"
 * that may be used for testing purposes.
 */
public class SailMasterLiveSimulatorConnectorImpl extends SailMasterConnectorForSocket {
    private final static Logger logger = Logger.getLogger(SailMasterLiveSimulatorConnectorImpl.class.getName());
    
    private final List<SailMasterMessage> bufferedMessageList;

    private long messageDeliveryIntervalInMs = Long.valueOf(System.getProperty("simulateLiveMode.delayInMillis", "250"));
    
    public SailMasterLiveSimulatorConnectorImpl(String host, int port, String raceId, String raceName, String raceDescription, BoatClass boatClass, SwissTimingRaceTrackerImpl swissTimingRaceTracker)
            throws InterruptedException, ParseException {
        super(host, port, raceId, raceName, raceDescription, boatClass, swissTimingRaceTracker); // causes original delivery to this.notifyListeners(...)
        bufferedMessageList = Collections.synchronizedList(new LinkedList<>());
        Thread messageDeliveryThread = new Thread("SailMasterLiveSimulatorConnector") {
            public void run() {
                while (true) {
                    try {
                        if (!bufferedMessageList.isEmpty()) {
                            SailMasterMessage message = bufferedMessageList.remove(0);
                            notifyParentListeners(message);
                        }
                        Thread.sleep(messageDeliveryIntervalInMs);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error trying to simulate SwissTiming messages", e);
                    }
                }
            }
        };
        messageDeliveryThread.start();
    }

    private void notifyParentListeners(SailMasterMessage message) throws ParseException {
        super.notifyListeners(message);
    }

    @Override
    protected void notifyListeners(SailMasterMessage message) {
        // we cache all messages here and deliver them with a different speed
        bufferedMessageList.add(message);
    }

}
