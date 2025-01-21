package com.sap.sailing.domain.igtimiadapter.server.replication;

import java.io.IOException;

import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotStandardCommand;
import com.sap.sse.common.TimePoint;

public interface ReplicableRiotServer extends RiotServer {

    Void internalRemoveDevice(long deviceId);

    Void internalUpdateDeviceName(long deviceId, String name);
    
    DataAccessWindow internalCreateDataAccessWindow(String deviceSerialNumber, TimePoint startTime, TimePoint endTime);

    Void internalRemoveDataAccessWindow(long dawId);

    Void internalNotifyListeners(Msg message, String deviceSerialNumber);

    Device internalCreateDevice(String deviceSerialNumber);

    boolean internalSendStandardCommand(String deviceSerialNumber, RiotStandardCommand command) throws IOException;

}
