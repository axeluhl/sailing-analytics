package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;

public class RRS26FinishingRaceFragment extends FinishingRaceFragment {
    
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getFragmentManager().beginTransaction().remove(positioningFragment).commit();
    }

}
