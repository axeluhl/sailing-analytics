package com.sap.sailing.server.trackfiles.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceBoatBravoExtendedMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorBravoExtendedMappingEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.sensordata.BravoExtendedSensorDataMetadata;
import com.sap.sse.common.TimePoint;

/**
 * Importer for CSV data files from Bravo units used by the SAP Extreme Sailing Team.
 */
public class BravoExtendedDataImporterImpl extends AbstractBravoDataImporterImpl {
    public static final String BRAVO_EXTENDED_TYPE = "BRAVO_EXTENDED";

    public BravoExtendedDataImporterImpl() {
        super(BRAVO_EXTENDED_TYPE, BravoExtendedSensorDataMetadata.getColumnNamesToIndexInDoubleFix());
    }

    @Override
    public RegattaLogDeviceCompetitorSensorDataMappingEvent createEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Competitor mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        return new RegattaLogDeviceCompetitorBravoExtendedMappingEventImpl(createdAt, logicalTimePoint, author, id, mappedTo,
                device, from, to);
    }
    
    @Override
    public RegattaLogDeviceBoatSensorDataMappingEvent createEvent(TimePoint createdAt, TimePoint logicalTimePoint,
            AbstractLogEventAuthor author, Serializable id, Boat mappedTo, DeviceIdentifier device,
            TimePoint from, TimePoint to) {
        return new RegattaLogDeviceBoatBravoExtendedMappingEventImpl(createdAt, logicalTimePoint, author, id, mappedTo,
                device, from, to);
    }
}
