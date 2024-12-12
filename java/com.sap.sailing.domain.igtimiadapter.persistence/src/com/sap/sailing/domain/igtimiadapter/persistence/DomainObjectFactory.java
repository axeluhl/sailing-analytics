package com.sap.sailing.domain.igtimiadapter.persistence;

import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;

public interface DomainObjectFactory {
    Iterable<Resource> getResources();

    Iterable<DataAccessWindow> getDataAccessWindows();

    Iterable<Device> getDevices();
}
