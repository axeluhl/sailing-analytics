package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sailing.domain.igtimiadapter.impl.ResourceImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;


/**
 * You can think of resources really as meta-info - identifying time blocks of data that have some meaning. For an
 * Igtimi device it's a power cycle. However data access is not forced to be "through" a resource. If you only wanted to
 * get data from a specific resource then of course you would just use the start and end time stamps in the request.
 * Resources are meant to be non-overlapping, although this is not strictly enforced.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface Resource extends HasId, HasStartAndEndTime, WithQualifiedObjectIdentifier {
    Iterable<Type> getDataTypes();

    String getDeviceSerialNumber();

    static Resource create(long id, TimePoint startTime, TimePoint endTime, String deviceSerialNumber, int[] dataTypes) {
        return new ResourceImpl(id, startTime, endTime, deviceSerialNumber, dataTypes);
    }
}
