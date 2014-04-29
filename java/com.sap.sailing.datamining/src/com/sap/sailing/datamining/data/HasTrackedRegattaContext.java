package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

public interface HasTrackedRegattaContext extends HasEventContext {
    
    @SideEffectFreeValue(messageKey="Regatta")
    public Regatta getRegatta();
    
    @SideEffectFreeValue(messageKey="CourseArea")
    public CourseArea getCourseArea();
    
    @SideEffectFreeValue(messageKey="BoatClass")
    public BoatClass getBoatClass();

}
