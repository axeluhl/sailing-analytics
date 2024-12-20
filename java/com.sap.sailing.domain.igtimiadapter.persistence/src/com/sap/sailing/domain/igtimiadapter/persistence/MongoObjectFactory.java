package com.sap.sailing.domain.igtimiadapter.persistence;

import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sse.common.TimePoint;

public interface MongoObjectFactory {
    void storeDevice(Device device);
    void removeDevice(long deviceId);
    void storeResource(Resource resource);
    void removeResource(long resourceId);
    void storeDataAccessWindow(DataAccessWindow daw);
    void removeDataAccessWindow(long dawId);
    
    /**
     * Tries to extract a time stamp from the {@code message} and stores the message's
     * binary protobuf data with the time stamp extracted (defaulting to {@link TimePoint#now()})
     * and the {@code serialNumber} to identify the device.
     * 
     * @param serialNumber if {@code null}, the message won't be stored
     */
    void storeMessage(String serialNumber, Msg message);
    void clear();
}
