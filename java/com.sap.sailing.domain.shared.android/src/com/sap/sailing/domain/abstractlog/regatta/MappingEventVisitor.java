package com.sap.sailing.domain.abstractlog.regatta;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;

/**
 * Used to dynamically dispatch calls for {@link RegattaLogDeviceMappingEvent}s depending on the concrete type.
 */
public interface MappingEventVisitor {

    void visit(RegattaLogDeviceMarkMappingEvent event);

    void visit(RegattaLogDeviceCompetitorMappingEvent event);
    
    void visit(RegattaLogDeviceBoatMappingEvent event);

    void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event);
    
    void visit(RegattaLogDeviceBoatSensorDataMappingEvent event);

}
