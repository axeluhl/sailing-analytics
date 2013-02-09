package com.sap.sailing.racecommittee.app.domain;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.common.Named;

public interface RaceGroup extends Named {
	
	public CourseArea getDefaultCourseArea();
	
	public BoatClass getBoatClass();
	
	public Iterable<SeriesWithRows> getSeries();

}
