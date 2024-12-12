package com.sap.sailing.domain.igtimiadapter.persistence;

import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;

public interface MongoObjectFactory {
    void storeDevice(Device device);
    void storeResource(Resource resource);
    void storeDataAccessWindow(DataAccessWindow daw);
    void clear();
}
