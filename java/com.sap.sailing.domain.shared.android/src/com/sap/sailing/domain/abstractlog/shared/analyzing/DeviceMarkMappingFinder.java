package com.sap.sailing.domain.abstractlog.shared.analyzing;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMarkMappingEvent;
import com.sap.sailing.domain.base.Mark;

public class DeviceMarkMappingFinder
<LogT extends AbstractLog<EventT, VisitorT>, EventT extends AbstractLogEvent<VisitorT>, VisitorT>
extends DeviceMappingFinder<LogT, EventT, VisitorT, Mark> {

    public DeviceMarkMappingFinder(LogT log) {
        super(log);
    }

    @Override
    protected boolean isValidMapping(DeviceMappingEvent<?, ?> mapping) {
        return mapping instanceof DeviceMarkMappingEvent;
    }
}
