package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.analyzing.DeviceMappingFinder;
import com.sap.sse.common.WithID;

public class RaceLogDeviceMappingFinder<ItemT extends WithID>
extends DeviceMappingFinder<RaceLog, RaceLogEvent, RaceLogEventVisitor, ItemT> {

    public RaceLogDeviceMappingFinder(RaceLog log) {
        super(log);
    }

}
