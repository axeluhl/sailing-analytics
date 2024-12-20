package com.sap.sailing.domain.igtimiadapter.persistence;

import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;

public interface MongoObjectFactory {
    void storeDevice(Device device);
    void removeDevice(long deviceId);
    void storeResource(Resource resource);
    void removeResource(long resourceId);
    void storeDataAccessWindow(DataAccessWindow daw);
    void removeDataAccessWindow(long dawId);
    void storeMessage(String serialNumber, Msg message);
    void clear();
}
