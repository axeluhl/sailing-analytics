package com.sap.sailing.domain.igtimiadapter.server.replication;

import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sailing.domain.igtimiadapter.server.riot.RiotServer;

public interface ReplicableRiotServer extends RiotServer {

    Void internalAddDevice(Device device);

    Void internalRemoveDevice(long deviceId);

    Void internalAddResource(Resource resource);

    Void internalRemoveResource(long resourceId);

    Void internalAddDataAccessWindow(DataAccessWindow daw);

    Void internalRemoveDataAccessWindow(long dawId);

    Void internalNotifyListeners(Msg message, String deviceSerialNumber);

    Device internalCreateDevice(String deviceSerialNumber);

}
