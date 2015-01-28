package com.sap.sailing.domain.abstractlog.shared.analyzing;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.BaseLogAnalyzer;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMappingEvent;
import com.sap.sse.common.WithID;

public class LastPingMappingEventFinder <LogT extends AbstractLog<EventT, VisitorT>,
EventT extends AbstractLogEvent<VisitorT>, VisitorT, ItemT extends WithID>
extends BaseLogAnalyzer<LogT, EventT, VisitorT, DeviceMappingEvent<VisitorT, ItemT>> {
    private final WithID item;
    
    public LastPingMappingEventFinder(LogT log, WithID forItem) {
        super(log);
        this.item = forItem;
    }

    @Override
    protected DeviceMappingEvent<VisitorT, ItemT> performAnalysis() {
        for (EventT e : getLog().getUnrevokedEventsDescending()) {
            if (e instanceof DeviceMappingEvent) {
                DeviceMappingEvent<?, ?> mappingEvent = (DeviceMappingEvent<?, ?>) e;
                if (item.equals(mappingEvent.getMappedTo()) && mappingEvent.getFrom() != null &&
                        mappingEvent.getTo() != null && mappingEvent.getFrom().equals(mappingEvent.getTo())) {
                    @SuppressWarnings("unchecked")
                    DeviceMappingEvent<VisitorT, ItemT> cast = (DeviceMappingEvent<VisitorT, ItemT>) mappingEvent;
                    return cast;
                }
            }
        }
        return null;
    }

}
