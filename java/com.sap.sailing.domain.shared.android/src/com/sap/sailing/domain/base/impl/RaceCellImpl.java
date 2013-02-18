package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.RaceCell;
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.racelog.RaceLog;

public class RaceCellImpl extends NamedImpl implements RaceCell {
	private static final long serialVersionUID = 971598420407273594L;
	
	private RaceLog raceLog;
	
	public RaceCellImpl(String name, RaceLog raceLog) {
		super(name);
		this.raceLog = raceLog;
	}

	@Override
	public RaceLog getRaceLog() {
		return raceLog;
	}

}
