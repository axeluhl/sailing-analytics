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
    
    @Connector(messageKey="Regatta")
    public Regatta getRegatta();
    
    @Connector(messageKey="CourseArea")
    public CourseArea getCourseArea();
    
    @Connector(messageKey="BoatClass")
    public BoatClass getBoatClass();
    
    @Connector(messageKey="Fleet")
    public Fleet getFleet();
    
    @Connector(messageKey="Race")
    public RaceDefinition getRace();
    
    @Dimension(messageKey="Year")
    public Integer getYear();

}