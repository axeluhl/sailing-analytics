package com.sap.sailing.datamining;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface GPSFixWithContext extends GPSFixMoving {
    
    public Competitor getCompetitor();
    public LegType getLegType();
    public TrackedRace getRace();
    public Regatta getRegatta();
    CourseArea getCourseArea();

}
