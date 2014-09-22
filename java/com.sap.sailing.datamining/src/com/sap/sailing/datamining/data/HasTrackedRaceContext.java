package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;

public interface HasTrackedRaceContext {
    
    public TrackedRace getTrackedRace();
    
    @Connector(messageKey="Regatta", ordinal=0)
    public Regatta getRegatta();
    
    @Connector(messageKey="CourseArea", ordinal=2)
    public CourseArea getCourseArea();
    
    @Connector(messageKey="BoatClass", ordinal=1)
    public BoatClass getBoatClass();
    
    @Connector(messageKey="Fleet", ordinal=3)
    public Fleet getFleet();
    
    @Connector(messageKey="Race", ordinal=4)
    public RaceDefinition getRace();
    
    @Dimension(messageKey="Year", ordinal=10)
    public Integer getYear();

}