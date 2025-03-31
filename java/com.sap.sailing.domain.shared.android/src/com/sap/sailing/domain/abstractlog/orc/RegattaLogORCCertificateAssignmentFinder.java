package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.base.Boat;

public class RegattaLogORCCertificateAssignmentFinder extends BaseORCCertificateAssignmentFinder<RegattaLog, RegattaLogEventVisitor, RegattaLogEvent> {
    public RegattaLogORCCertificateAssignmentFinder(RegattaLog log, Map<Serializable, Boat> boatsById) {
        super(log, boatsById);
    }
}
