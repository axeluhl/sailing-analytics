package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.base.Boat;

public class RaceLogORCCertificateAssignmentFinder
extends BaseORCCertificateAssignmentFinder<RaceLog, RaceLogEventVisitor, RaceLogEvent> {

    public RaceLogORCCertificateAssignmentFinder(RaceLog log, Map<Serializable, Boat> boatsById) {
        super(log, boatsById);
    }
}
