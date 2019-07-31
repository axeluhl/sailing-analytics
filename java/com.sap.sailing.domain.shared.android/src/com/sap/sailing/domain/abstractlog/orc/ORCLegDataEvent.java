package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;

// TODO Comments
public interface ORCLegDataEvent extends RaceLogEvent {

    public Distance getLength();
    
    public Bearing getTwa();
    
    public int getLegNr();
    
}
