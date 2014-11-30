package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.DeviceMappingEvent;
import com.sap.sse.common.WithID;

public class LastPingMappingEventFinder<T extends WithID> extends RaceLogAnalyzer<DeviceMappingEvent<T>> {
    private final WithID item;
    
    public LastPingMappingEventFinder(RaceLog raceLog, WithID forItem) {
        super(raceLog);
        this.item = forItem;
    }

    @Override
    protected DeviceMappingEvent<T> performAnalysis() {
        for (RaceLogEvent e : getLog().getUnrevokedEventsDescending()) {
            if (e instanceof DeviceMappingEvent) {
                DeviceMappingEvent<?> mappingEvent = (DeviceMappingEvent<?>) e;
                if (item.equals(mappingEvent.getMappedTo()) && mappingEvent.getFrom() != null &&
                        mappingEvent.getTo() != null && mappingEvent.getFrom().equals(mappingEvent.getTo())) {
                    @SuppressWarnings("unchecked")
                    DeviceMappingEvent<T> cast = (DeviceMappingEvent<T>) mappingEvent;
                    return cast;
                }
            }
        }
        return null;
    }

}
