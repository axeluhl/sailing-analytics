package com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sse.common.WithID;

public class LastPingMappingEventFinder <ItemT extends WithID> extends RegattaLogAnalyzer<RegattaLogDeviceMappingEvent<ItemT>> {
    private final WithID item;
    
    public LastPingMappingEventFinder(RegattaLog log, WithID forItem) {
        super(log);
        this.item = forItem;
    }

    @Override
    protected RegattaLogDeviceMappingEvent<ItemT> performAnalysis() {
        for (RegattaLogEvent e : getLog().getUnrevokedEventsDescending()) {
            if (e instanceof RegattaLogDeviceMappingEvent) {
                RegattaLogDeviceMappingEvent<?> mappingEvent = (RegattaLogDeviceMappingEvent<?>) e;
                if (item.equals(mappingEvent.getMappedTo()) && mappingEvent.getFrom() != null &&
                        mappingEvent.getTo() != null && mappingEvent.getFrom().equals(mappingEvent.getTo())) {
                    @SuppressWarnings("unchecked")
                    RegattaLogDeviceMappingEvent<ItemT> cast = (RegattaLogDeviceMappingEvent<ItemT>) mappingEvent;
                    return cast;
                }
            }
        }
        return null;
    }

}
