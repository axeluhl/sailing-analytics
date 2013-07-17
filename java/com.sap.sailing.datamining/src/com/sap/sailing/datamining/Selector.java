package com.sap.sailing.datamining;

import java.util.List;

import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.server.RacingEventService;

public interface Selector {
    
    public List<String> getXValues();
    
    public List<GPSFixMoving> getDataFor(String xValue);

    void initializeSelection(RacingEventService racingEventService);

}
