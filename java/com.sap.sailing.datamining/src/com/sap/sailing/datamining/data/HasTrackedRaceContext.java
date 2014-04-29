package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface HasTrackedRaceContext extends HasLeaderboardContext {
    
    public TrackedRegatta getTrackedRegatta();
    public TrackedRace getTrackedRace();

    @SideEffectFreeValue(messageKey="Regatte")
    public Regatta getRegatta();
    
    @SideEffectFreeValue(messageKey="CourseArea")
    public CourseArea getCourseArea();
    
    @SideEffectFreeValue(messageKey="Fleet")
    public Fleet getFleet();
    
    @SideEffectFreeValue(messageKey="BoatClass")
    public BoatClass getBoatClass();
    
    @SideEffectFreeValue(messageKey="Race")
    public RaceDefinition getRace();
    
    @Dimension(messageKey="Year")
    public Integer getYear();

}