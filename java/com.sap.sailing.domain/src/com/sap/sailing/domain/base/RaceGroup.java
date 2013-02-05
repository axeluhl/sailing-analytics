package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;

public interface RaceGroup extends Named {
	
	public CourseArea getDefaultCourseArea();
	
	public Iterable<SeriesData> getSeries();

}
