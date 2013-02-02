package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;

public class RaceLogCourseAreaChangeEventImpl extends RaceLogEventImpl implements
		RaceLogCourseAreaChangedEvent {
	private static final long serialVersionUID = -3943108136024977860L;
	
	private Serializable courseAreaId;

	public RaceLogCourseAreaChangeEventImpl(TimePoint pTimePoint,
			Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Serializable courseAreaId) {
		super(pTimePoint, pId, pInvolvedBoats, pPassId);
		this.courseAreaId = courseAreaId;
	}

	@Override
	public Serializable getCourseAreaId() {
		return courseAreaId;
	}

}
