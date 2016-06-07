package com.sap.sailing.domain.abstractlog.regatta;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;

public interface EventMappingVisitor {

    void visit(RegattaLogDeviceMarkMappingEvent event);

    void visit(RegattaLogDeviceCompetitorMappingEvent event);

    void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event);

}
