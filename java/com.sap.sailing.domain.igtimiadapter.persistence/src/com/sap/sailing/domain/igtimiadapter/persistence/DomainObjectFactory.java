package com.sap.sailing.domain.igtimiadapter.persistence;

import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sse.common.TimeRange;

public interface DomainObjectFactory {
    Iterable<Resource> getResources();

    Iterable<DataAccessWindow> getDataAccessWindows();

    Iterable<Device> getDevices();
    
    Iterable<Msg> getMessages(String deviceSerialNumber, TimeRange timeRange);
}
