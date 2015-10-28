package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.CloseOpenEndedDeviceMappingEvent;

@Deprecated //bug2851
public interface RaceLogCloseOpenEndedDeviceMappingEvent extends RaceLogEvent,
CloseOpenEndedDeviceMappingEvent<RaceLogEventVisitor> {

}
