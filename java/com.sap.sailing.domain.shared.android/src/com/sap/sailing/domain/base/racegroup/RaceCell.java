package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.racelog.RaceLog;

public interface RaceCell extends Named {
	
	RaceLog getRaceLog();

}
