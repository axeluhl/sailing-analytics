package com.sap.sailing.racecommittee.app.domain;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.common.Named;

public interface RaceGroup extends Named {
	
	public CourseArea getDefaultCourseArea();
	
	public Iterable<SeriesWithRows> getSeries();

}
