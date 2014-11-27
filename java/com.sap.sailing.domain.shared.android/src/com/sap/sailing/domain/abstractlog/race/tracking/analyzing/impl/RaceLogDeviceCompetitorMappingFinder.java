package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.analyzing.DeviceCompetitorMappingFinder;

public class RaceLogDeviceCompetitorMappingFinder
extends DeviceCompetitorMappingFinder<RaceLog, RaceLogEvent, RaceLogEventVisitor> {

    public RaceLogDeviceCompetitorMappingFinder(RaceLog log) {
        super(log);
    }

}
