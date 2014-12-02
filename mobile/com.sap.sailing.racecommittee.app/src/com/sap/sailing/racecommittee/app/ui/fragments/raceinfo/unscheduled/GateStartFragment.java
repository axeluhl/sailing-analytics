package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class GateStartFragment extends RaceFragment{
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_procedure_gate_start, container, false);
        
        return view;
    }
    
}
