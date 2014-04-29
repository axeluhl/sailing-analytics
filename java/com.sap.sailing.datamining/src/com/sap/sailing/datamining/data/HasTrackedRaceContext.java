package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface HasTrackedRaceContext extends HasTrackedRegattaContext {
    
    public TrackedRace getTrackedRace();
    
    @SideEffectFreeValue(messageKey="Fleet")
    public Fleet getFleet();
    
    @SideEffectFreeValue(messageKey="Race")
    public RaceDefinition getRace();
    
    @Dimension(messageKey="Year")
    public Integer getYear();

}