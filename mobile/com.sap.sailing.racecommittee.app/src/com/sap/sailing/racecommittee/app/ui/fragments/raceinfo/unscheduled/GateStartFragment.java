package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class GateStartFragment extends RaceFragment{
    
    public GateStartFragment() {
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_procedure_gate_start, container, false);
        
        Button header = (Button) view.findViewById(R.id.header);
        if (header != null) {
            header.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    RacingActivity activity = (RacingActivity) getActivity();
                    if (activity != null) {
                        activity.replaceFragment(new StartProcedureChangeFragment());
                    }
                }
            });
        }
        
        return view;
    }
    
}
