package com.sap.sailing.server.trackfiles.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceBoatBravoMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoMappingEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sse.common.TimePoint;

/**
 * Importer for CSV data files from Bravo units used at the ESS.
 */
public class BravoDataImporterImpl extends AbstractBravoDataImporterImpl {
    public static final String BRAVO_TYPE = "BRAVO";

    public BravoDataImporterImpl() {
        super(BRAVO_TYPE, BravoSensorDataMetadata.getColumnNamesToIndexInDoubleFix());
    }

    @Override
    public RegattaLogDeviceCompetitorSensorDataMappingEvent createEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Competitor mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        return new RegattaLogDeviceCompetitorBravoMappingEventImpl(createdAt, logicalTimePoint, author, id, mappedTo,
                device, from, to);
    }
    
    @Override
    public RegattaLogDeviceBoatSensorDataMappingEvent createEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Boat mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        return new RegattaLogDeviceBoatBravoMappingEventImpl(createdAt, logicalTimePoint, author, id, mappedTo,
                device, from, to);
    }
}
