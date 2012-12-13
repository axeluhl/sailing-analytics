package com.sap.sailing.domain.racecommittee.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;

public abstract class RaceCommitteeEventImpl implements RaceCommitteeEvent {

	private static final long serialVersionUID = -5810258278984777732L;
	
	private TimePoint timePoint;
	private Serializable id;
	private List<Competitor> involvedBoats;
	private int passId;
	
	public RaceCommitteeEventImpl(TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
		this.timePoint = pTimePoint;
		this.id = pId;
		this.involvedBoats = pInvolvedBoats;
		this.passId = pPassId;
	}

	@Override
	public TimePoint getTimePoint() {
		return timePoint;
	}

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public List<Competitor> getInvolvedBoats() {
		return involvedBoats;
	}

	@Override
	public int getPassId() {
		return passId;
	}

}
