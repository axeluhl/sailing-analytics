package com.sap.sailing.racecommittee.app.ui.fragments.chooser;

import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.DummyInfoFragment;

public class RaceInfoFragmentChooser {
	private static final String TAG = RaceInfoFragmentChooser.class.getName();
	
	public RaceFragment choose(ManagedRace managedRace) {
		return createForRace(DummyInfoFragment.class, managedRace);
	}

	private RaceFragment createForRace(Class<? extends RaceFragment> fragmentClass, ManagedRace managedRace) {
		try {
			RaceFragment fragment = fragmentClass.newInstance();
			fragment.setArguments(RaceFragment.createArguments(managedRace));
			return fragment;
		} catch (Exception e) {
			ExLog.e(TAG, String.format("Exception while instantiating race info fragment:\n%s", e.toString()));
			return new DummyInfoFragment();
		}
	}

}
