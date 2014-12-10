package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.gatestart;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.NextFragmentListener;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PathFinderFragment extends RaceFragment{

    NextFragmentListener mListener;
    
    public PathFinderFragment() {
        
    }
    
    public PathFinderFragment(NextFragmentListener listener) {
        mListener = listener;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_procedure_gate_start_pathfinder, container, false);
        return view;
    }
}
