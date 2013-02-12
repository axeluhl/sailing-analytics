package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;

public class RaceLogRaceStatusEventImpl extends RaceLogEventImpl implements
		RaceLogRaceStatusEvent {
	private static final long serialVersionUID = -8809758843066724482L;
	
	protected RaceLogRaceStatus nextStatus;

	public RaceLogRaceStatusEventImpl(
			TimePoint pTimePoint, 
			Serializable pId,
			List<Competitor> pInvolvedBoats, 
			int pPassId,
			RaceLogRaceStatus nextStatus) {
		super(pTimePoint, pId, pInvolvedBoats, pPassId);
		this.nextStatus = nextStatus;
	}

	@Override
	public RaceLogRaceStatus getNextStatus() {
		return nextStatus;
	}
	
	

}
