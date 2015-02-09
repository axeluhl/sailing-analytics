package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceStartTimeFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.gatestart.GateTimingFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.gatestart.PathFinderFragment;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GateStartFragment extends RaceFragment{
    
    private int mTab = -1;
    
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
                        RaceFragment fragment = new StartProcedureChangeFragment();
                        fragment.setArguments(getArguments());
                        activity.replaceFragment(fragment);
                    }
                }
            });
        }
        
        final Button pathFinder = (Button) view.findViewById(R.id.pathfinder);
        if (pathFinder != null) {
            pathFinder.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    mTab = -1;
                }
            });
        }
        
        final Button gateTiming = (Button) view.findViewById(R.id.gate_timing);
        if (gateTiming != null) {
            gateTiming.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    mTab = 0;
                }
            });
        }
        
        final Button startTime = (Button) view.findViewById(R.id.start_time);
        if (startTime != null) {
            startTime.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    mTab = 1;
                }
            });
        }
        
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        getRaceState().setRacingProcedure(MillisecondsTimePoint.now(), RacingProcedureType.GateStart);
    }
    
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
        fragment.setArguments(getArguments());
        transaction.replace(R.id.gate_sub_fragment, fragment);
        transaction.commit();
    }
}
