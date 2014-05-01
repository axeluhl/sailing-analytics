package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface HasGPSFixContext extends HasTrackedLegOfCompetitorContext {
    
    @SideEffectFreeValue(messageKey="GPSFix")
    public GPSFixMoving getGPSFix();

}