package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class RaceLogStartTimeEventImpl extends RaceLogEventImpl
		implements RaceLogStartTimeEvent {

	private static final long serialVersionUID = 8185811395997196162L;
	private TimePoint startTime;
	
	public RaceLogStartTimeEventImpl(TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, TimePoint pStartTime) {
		super(pTimePoint, pId, pInvolvedBoats, pPassId);
		this.startTime = pStartTime;
	}

	@Override
	public TimePoint getStartTime() {
		return startTime;
	}
	
	@Override
	public boolean equals(Object object) {
		return super.equals(object) 
				&& object instanceof RaceLogStartTimeEvent 
				&& startTime.equals(((RaceLogStartTimeEvent) object).getStartTime());
	}

}
