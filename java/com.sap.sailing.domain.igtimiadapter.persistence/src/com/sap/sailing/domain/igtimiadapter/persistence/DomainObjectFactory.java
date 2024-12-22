package com.sap.sailing.domain.igtimiadapter.persistence;

import java.util.Set;

import com.google.protobuf.InvalidProtocolBufferException;
import com.igtimi.IgtimiData.DataPoint;
import com.igtimi.IgtimiData.DataPoint.DataCase;
import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.igtimiadapter.DataAccessWindow;
import com.sap.sailing.domain.igtimiadapter.Device;
import com.sap.sailing.domain.igtimiadapter.Resource;
import com.sap.sse.common.TimeRange;

public interface DomainObjectFactory {
    Iterable<Resource> getResources();

    Iterable<DataAccessWindow> getDataAccessWindows();

    Iterable<Device> getDevices();
    
    Iterable<Msg> getMessages(String deviceSerialNumber, TimeRange timeRange, Set<DataCase> dataCases);

    /**
     * Finds the latest message received from the device identified by {@code deviceSerialNumber}
     * that has a {@link DataPoint} of the correct {@link DataCase}.
     */
    Msg getLatestMessage(String deviceSerialNumber, DataCase dataCase) throws InvalidProtocolBufferException;
}
