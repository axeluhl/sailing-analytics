package com.sap.sailing.domain.racecommittee.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;

public class RaceCommitteeStartTimeEventImpl extends RaceCommitteeEventImpl
		implements RaceCommitteeStartTimeEvent {

	private static final long serialVersionUID = 8185811395997196162L;
	private TimePoint startTime;
	
	public RaceCommitteeStartTimeEventImpl(TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, TimePoint pStartTime) {
		super(pTimePoint, pId, pInvolvedBoats, pPassId);
		this.startTime = pStartTime;
	}

	@Override
	public TimePoint getStartTime() {
		return startTime;
	}

}
