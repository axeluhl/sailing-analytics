package com.sap.sailing.domain.abstractlog.regatta;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;

public interface RegattaLogEventVisitor {
    void visit(RegattaLogRevokeEvent event);

    void visit(RegattaLogDeviceMarkMappingEvent event);

    void visit(RegattaLogDeviceCompetitorMappingEvent event);
    
    void visit(RegattaLogDeviceBoatMappingEvent event);

    void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event);
    
    void visit(RegattaLogDeviceBoatSensorDataMappingEvent event);

    void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event);

    void visit(RegattaLogRegisterBoatEvent event);

    void visit(RegattaLogRegisterCompetitorEvent event);

    void visit(RegattaLogSetCompetitorTimeOnTimeFactorEvent event);
    
    void visit(RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent event);

    void visit(RegattaLogDefineMarkEvent event);
}
