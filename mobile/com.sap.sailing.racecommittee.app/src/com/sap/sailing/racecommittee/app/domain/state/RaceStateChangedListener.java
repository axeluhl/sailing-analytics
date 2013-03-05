package com.sap.sailing.racecommittee.app.domain.state;

public interface RaceStateChangedListener {
	
	void onRaceStateChanged(RaceState state);

}
