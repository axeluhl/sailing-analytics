package com.sap.sailing.domain.abstractlog.regatta;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;

public interface RegattaLogEventVisitor {
    void visit(RegattaLogRevokeEvent event);

    void visit(RegattaLogDeviceMarkMappingEvent event);

    void visit(RegattaLogDeviceCompetitorMappingEvent event);

    void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event);

    void visit(RegattaLogRegisterCompetitorEvent event);
}
