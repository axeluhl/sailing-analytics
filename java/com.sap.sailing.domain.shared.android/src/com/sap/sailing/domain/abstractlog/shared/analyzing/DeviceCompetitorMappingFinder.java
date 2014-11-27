package com.sap.sailing.domain.abstractlog.shared.analyzing;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sailing.domain.base.Competitor;

public class DeviceCompetitorMappingFinder<LogT extends AbstractLog
<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
extends DeviceMappingFinder<LogT, EventT, VisitorT, Competitor> {

    public DeviceCompetitorMappingFinder(LogT log) {
        super(log);
    }

    @Override
    protected boolean isValidMapping(DeviceMappingEvent<?, ?> mapping) {
        return mapping instanceof DeviceCompetitorMappingEvent;
    }
}
