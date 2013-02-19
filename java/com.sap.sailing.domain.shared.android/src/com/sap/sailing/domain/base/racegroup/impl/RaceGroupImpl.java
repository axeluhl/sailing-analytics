package com.sap.sailing.domain.base.racegroup.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.common.impl.NamedImpl;


public class RaceGroupImpl extends NamedImpl implements RaceGroup {
	private static final long serialVersionUID = 7760879536339600827L;
	
	private final BoatClass boatClass;
	private final CourseArea courseArea;
	private final Iterable<SeriesWithRows> series;
	
	public RaceGroupImpl(
			String name,
			BoatClass boatClass,
			CourseArea courseArea,
			Iterable<SeriesWithRows> series) {
		super(name);
		this.boatClass = boatClass;
		this.courseArea = courseArea;
		this.series = series;
	}

	public CourseArea getDefaultCourseArea() {
		return courseArea;
	}

	public Iterable<SeriesWithRows> getSeries() {
		return series;
	}

	public BoatClass getBoatClass() {
		return boatClass;
	}

}
