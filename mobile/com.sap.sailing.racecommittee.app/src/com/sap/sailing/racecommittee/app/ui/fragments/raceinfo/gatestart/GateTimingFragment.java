package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.gatestart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.NextFragmentListener;

public class GateTimingFragment extends RaceFragment {
    
    private NextFragmentListener mListener;
    
    public GateTimingFragment() {
        
    }
    
    public GateTimingFragment(NextFragmentListener listener) {
        mListener = listener;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_procedure_gate_start_timing, container, false);
        return view;
    }
}
