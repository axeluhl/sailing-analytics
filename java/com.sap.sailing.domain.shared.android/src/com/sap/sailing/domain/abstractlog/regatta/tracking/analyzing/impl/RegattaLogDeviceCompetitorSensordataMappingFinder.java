package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sse.common.WithID;

public class RegattaLogDeviceCompetitorSensordataMappingFinder<ItemT extends WithID> extends RegattaLogDeviceMappingFinder<ItemT> {

    public RegattaLogDeviceCompetitorSensordataMappingFinder(RegattaLog log) {
        super(log);
    }

    @Override
    protected boolean isValidMapping(RegattaLogDeviceMappingEvent<?> mapping) {
        return mapping instanceof RegattaLogDeviceMappingEvent;
    }

}
