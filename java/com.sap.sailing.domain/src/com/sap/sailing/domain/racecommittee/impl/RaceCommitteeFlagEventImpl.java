package com.sap.sailing.domain.racecommittee.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.Flags;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;

public class RaceCommitteeFlagEventImpl extends RaceCommitteeEventImpl implements RaceCommitteeFlagEvent {
	
	private static final long serialVersionUID = 6333303528852541914L;
	private Flags upperFlag;
	private Flags lowerFlag;
	private boolean isDisplayed;

	public RaceCommitteeFlagEventImpl(TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Flags pUpperFlag, Flags pLowerFlag, boolean pIsDisplayed) {
		super(pTimePoint, pId, pInvolvedBoats, pPassId);
		this.upperFlag = pUpperFlag;
		this.lowerFlag = pLowerFlag;
		this.isDisplayed = pIsDisplayed;
	}

	@Override
	public Flags getUpperFlag() {
		return upperFlag;
	}

	@Override
	public Flags getLowerFlag() {
		return lowerFlag;
	}

	@Override
	public boolean isDisplayed() {
		return isDisplayed;
	}

}
