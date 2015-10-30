package com.sap.sailing.domain.abstractlog.race.tracking;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceMarkMappingEvent;

@Deprecated //(see bug2851)
public interface RaceLogDeviceMarkMappingEvent extends RaceLogEvent,
DeviceMarkMappingEvent<RaceLogEventVisitor> {

}
