package com.sap.sailing.domain.swisstimingadapter.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.swisstimingadapter.SailMasterMessage;

public class SailMasterLiveSimulatorConnectorImpl extends SailMasterConnectorImpl {
    private final List<SailMasterMessage> bufferedMessageList;

    private long messageDeliveryIntervalInMs = Long.valueOf(System.getProperty("simulateLiveMode.delayInMillis", "250"));
    
    public SailMasterLiveSimulatorConnectorImpl(String host, int port, String raceId, String raceName, String raceDescription, BoatClass boatClass)
            throws InterruptedException, ParseException {
        super(host, port, raceId, raceName, raceDescription, boatClass);
        bufferedMessageList = Collections.synchronizedList(new ArrayList<SailMasterMessage>());
        Thread messageDeliveryThread = new Thread("SailMasterLiveSimulatorConnector") {
            public void run() {
                while(true) {
                    try {
                        if(!bufferedMessageList.isEmpty()) {
                            SailMasterMessage message = bufferedMessageList.get(0);
                            notifyParentListeners(message);
                            bufferedMessageList.remove(0);
                        }                    
                        Thread.sleep(messageDeliveryIntervalInMs);
                    } catch (Exception e) {
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
