package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.analyzing.DeviceMarkMappingFinder;

public class RaceLogDeviceMarkMappingFinder
extends DeviceMarkMappingFinder<RaceLog, RaceLogEvent, RaceLogEventVisitor> {

    public RaceLogDeviceMarkMappingFinder(RaceLog log) {
        super(log);
    }

}
