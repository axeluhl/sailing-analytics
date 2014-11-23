package com.sap.sailing.server.gateway.jaxrs.api;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.TimePoint;

public interface WindTrackJsonSerializer extends JsonSerializer<WindTrack> {
    void setFromTime(TimePoint fromTime);
    void setToTime(TimePoint toTime);
    void setWindSource(WindSource windSource);
}
