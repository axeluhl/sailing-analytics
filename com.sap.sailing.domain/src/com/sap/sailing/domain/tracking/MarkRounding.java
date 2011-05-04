package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Timed;

public interface MarkRounding extends Timed {
	Mark getMark();
	Boat getBoat();
}
