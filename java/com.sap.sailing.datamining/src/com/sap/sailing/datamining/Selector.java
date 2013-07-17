package com.sap.sailing.datamining;

import java.util.List;

import com.sap.sailing.server.RacingEventService;

public interface Selector {

    public List<GPSFixWithContext> selectGPSFixes(RacingEventService racingEventService);

}
