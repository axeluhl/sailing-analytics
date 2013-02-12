package com.sap.sailing.domain.racelog.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;

public class RaceLogPassChangeEventImpl extends RaceLogEventImpl implements RaceLogPassChangeEvent {
	private static final long serialVersionUID = -3737606977320640630L;

	private int newPassId;
	
	public RaceLogPassChangeEventImpl(TimePoint pTimePoint, Serializable pId,
			List<Competitor> pInvolvedBoats, int pPassId, int newPassId) {
		super(pTimePoint, pId, pInvolvedBoats, pPassId);
		this.newPassId = newPassId;
	}

	@Override
	public int getNewPassId() {
		return newPassId;
	}

}
