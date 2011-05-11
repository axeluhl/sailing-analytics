package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Boat;

public interface BoatGPSFix extends GPSFix {
	Boat getBoat();
}
