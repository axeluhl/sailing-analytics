package com.sap.sailing.domain.racecommittee.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.RaceCommitteeCourseAreaChangedEvent;

public class RaceCommitteeCourseAreaChangeEventImpl extends RaceCommitteeEventImpl implements
		RaceCommitteeCourseAreaChangedEvent {
	private static final long serialVersionUID = -3943108136024977860L;
	
	private Serializable courseAreaId;

	public RaceCommitteeCourseAreaChangeEventImpl(TimePoint pTimePoint,
			Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Serializable courseAreaId) {
		super(pTimePoint, pId, pInvolvedBoats, pPassId);
		this.courseAreaId = courseAreaId;
	}

	@Override
	public Serializable getCourseAreaId() {
		return courseAreaId;
	}

}
