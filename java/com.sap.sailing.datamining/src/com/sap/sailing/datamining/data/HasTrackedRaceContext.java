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
    
    @Connector
    public Regatta getRegatta();
    
    @Connector
    public CourseArea getCourseArea();
    
    @Connector
    public BoatClass getBoatClass();
    
    @Connector
    public Fleet getFleet();
    
    @Connector
    public RaceDefinition getRace();
    
    @Dimension(messageKey="Year")
    public Integer getYear();

}