package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public class CourseFragment extends RaceFragment {
    
    public CourseFragment() {
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_course, container, false);
        
        Button confirm = (Button) view.findViewById(R.id.confirm);

        return view;
    }
}
