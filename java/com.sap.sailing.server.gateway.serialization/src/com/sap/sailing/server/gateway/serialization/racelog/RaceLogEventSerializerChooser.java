package com.sap.sailing.server.gateway.serialization.racelog;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public interface RaceLogEventSerializerChooser {
    JsonSerializer<RaceLogEvent> getSerializer(RaceLogEvent event);
}
