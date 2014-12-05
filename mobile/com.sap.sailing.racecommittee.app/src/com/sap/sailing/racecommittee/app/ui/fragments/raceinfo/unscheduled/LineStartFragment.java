package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.NextFragmentListener;

public class LineStartFragment extends RaceFragment {

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
                        activity.replaceFragment(new StartProcedureChangeFragment());
                    }
                }
            });
        }
        
        Button confirm = (Button) view.findViewById(R.id.confirm);
        if (confirm != null) {
            confirm.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Save data
                    mListener.nextFragment();
                }
            });
        }

        return view;
    }
}
