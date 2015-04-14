package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.sap.sailing.racecommittee.app.R;

public class CourseFragmentMap extends CourseFragment {

    private static final String TAG = CourseFragmentMap.class.getName();

    public static CourseFragmentMap newInstance(int startMode) {
        CourseFragmentMap fragment = new CourseFragmentMap();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_course_map, container, false);
        
        Button confirm = (Button) view.findViewById(R.id.confirm);

        return view;
    }
}
