package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public abstract class RaceLogEventImpl implements RaceLogEvent {

	private static final long serialVersionUID = -5810258278984777732L;
	
	private TimePoint timePoint;
	private Serializable id;
	private List<Competitor> involvedBoats;
	private int passId;
	
	public RaceLogEventImpl(TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId) {
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
	
	@Override
	public boolean equals(Object object) {
		return object instanceof RaceLogEvent 
				&& timePoint.equals(((RaceLogEvent) object).getTimePoint())
				&& id.equals(((RaceLogEvent) object).getId())
				&& involvedBoats.equals(((RaceLogEvent) object).getInvolvedBoats())
				&& passId == ((RaceLogEvent) object).getPassId();
	}

}
