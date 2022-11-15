package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import com.sap.sailing.racecommittee.app.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CourseFragmentMap extends CourseFragment {

    public static CourseFragmentMap newInstance(@START_MODE_VALUES int startMode) {
        CourseFragmentMap fragment = new CourseFragmentMap();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_course_map, container, false);

        return view;
    }
}
