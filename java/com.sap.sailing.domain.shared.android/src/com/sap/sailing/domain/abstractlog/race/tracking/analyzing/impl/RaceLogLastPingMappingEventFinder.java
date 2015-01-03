package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.analyzing.LastPingMappingEventFinder;
import com.sap.sse.common.WithID;

public class RaceLogLastPingMappingEventFinder<ItemT extends WithID>
extends LastPingMappingEventFinder<RaceLog, RaceLogEvent, RaceLogEventVisitor, ItemT> {

    public RaceLogLastPingMappingEventFinder(RaceLog log, WithID forItem) {
        super(log, forItem);
    }

}
