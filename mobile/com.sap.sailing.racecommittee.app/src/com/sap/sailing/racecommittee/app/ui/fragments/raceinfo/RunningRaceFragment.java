package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class RunningRaceFragment extends RaceFragment {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_running_view, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        ImageButton blueFlagButton = (ImageButton) getView().findViewById(R.id.blueFlagButton);
        blueFlagButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getRace().getState().onRaceFinishing(MillisecondsTimePoint.now());
            }
        });
    }

}
