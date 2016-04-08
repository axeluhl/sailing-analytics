package com.sap.sailing.domain.trackimport;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sse.common.TimePoint;

public interface DoubleVectorFixImporter {    

    interface Callback {
        void addFix(DoubleVectorFix fix, TrackFileImportDeviceIdentifier device);
    }

    void importFixes(InputStream inputStream, Callback callback, String sourceName)
            throws FormatNotSupportedException, IOException;

    String getType();
    
    RegattaLogDeviceCompetitorSensorDataMappingEvent createEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Competitor mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to);
    
}
