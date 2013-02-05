package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.RaceGroup;
import com.sap.sailing.domain.base.SeriesData;

public class RaceGroupImpl implements RaceGroup {
	private static final long serialVersionUID = 7760879536339600827L;
	
	private final String name;
	private final CourseArea courseArea;
	private final Iterable<SeriesData> series;
	
	public RaceGroupImpl(
			String name, 
			CourseArea courseArea,
			Iterable<SeriesData> series) {
		this.name = name;
		this.courseArea = courseArea;
		this.series = series;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CourseArea getDefaultCourseArea() {
		return courseArea;
	}

	@Override
	public Iterable<SeriesData> getSeries() {
		return series;
	}

}
