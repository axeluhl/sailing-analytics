package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

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
import com.sap.sailing.racecommittee.app.utils.NextFragmentListener;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LineStartFragment extends RaceFragment implements TickListener {

    private static final String TAG = LineStartFragment.class.getName();
    
    private NextFragmentListener mListener;

    public LineStartFragment() {
        
    }
    
    public LineStartFragment(NextFragmentListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_procedure_line_start, container, false);

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
        
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        getRaceState().setRacingProcedure(MillisecondsTimePoint.now(), RacingProcedureType.RRS26);
        
        RaceFragment fragment = new RaceStartTimeFragment(mListener);
        fragment.setArguments(getArguments());
        getActivity().getFragmentManager().beginTransaction().replace(R.id.line_sub_fragment, fragment).commit();
    }
}
