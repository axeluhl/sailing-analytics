package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public class RaceLogFlagEventImpl extends RaceLogEventImpl implements RaceLogFlagEvent {
	
	private static final long serialVersionUID = 6333303528852541914L;
	private Flags upperFlag;
	private Flags lowerFlag;
	private boolean isDisplayed;

	public RaceLogFlagEventImpl(TimePoint pTimePoint, Serializable pId, List<Competitor> pInvolvedBoats, int pPassId, Flags pUpperFlag, Flags pLowerFlag, boolean pIsDisplayed) {
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
	
	@Override
	public void accept(RaceLogEventVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public boolean equals(Object object) {
		return super.equals(object) 
				&& object instanceof RaceLogFlagEvent 
				&& upperFlag.equals(((RaceLogFlagEvent) object).getUpperFlag())
				&& lowerFlag.equals(((RaceLogFlagEvent) object).getLowerFlag())
				&& isDisplayed == ((RaceLogFlagEvent) object).isDisplayed();
	}

}
