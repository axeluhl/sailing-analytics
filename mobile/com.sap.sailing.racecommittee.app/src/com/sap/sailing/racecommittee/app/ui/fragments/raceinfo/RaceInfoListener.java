package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import com.sap.sailing.racecommittee.app.ui.fragments.RaceInfoFragment;

/**
 * Interface to enable communication between different Fragments loaded by the {@link RaceInfoFragment}.
 */
public interface RaceInfoListener {

	/**
	 * Called when a fragment wants a race's start time to be reseted.
	 */
	void onResetTime();
}
