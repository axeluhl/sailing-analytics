package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.NextFragmentListener;

public class MainScheduleFragment extends RaceFragment implements OnClickListener, NextFragmentListener {

    private Button mCourse;
    private RaceFragment mCurrent;
    private TextView mHeaderText;
    private Button mStartMode;
    private Button mStartProcedure;

    private int mTab = 0;

    @Override
    public void nextFragment() {
        mTab++;

        if (mTab > 2) {
            mTab = 0;
        }
        openTabFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHeaderText = (TextView) getView().findViewById(R.id.header_text);
        String serie = managedRace.getSeries().getName();
        String fleet = managedRace.getFleet().getName();
        String race = managedRace.getRaceName();
        mHeaderText.setText(serie + " - " + fleet + " - " + race);

        openTabFragment();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.start_procedure:
            mTab = 0;
            break;

        case R.id.start_mode:
            mTab = 1;
            break;

        case R.id.course:
            mTab = 2;
            break;
        }

        openTabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule, container, false);

        mCourse = (Button) view.findViewById(R.id.course);
        if (mCourse != null) {
            mCourse.setOnClickListener(this);
        }

        mStartMode = (Button) view.findViewById(R.id.start_mode);
        if (mStartMode != null) {
            mStartMode.setOnClickListener(this);
        }

        mStartProcedure = (Button) view.findViewById(R.id.start_procedure);
        if (mStartProcedure != null) {
            mStartProcedure.setOnClickListener(this);
        }

        return view;
    }

    private void openFragment() {
        getFragmentManager().beginTransaction().replace(R.id.sub_fragment, mCurrent).commit();
    }

    private void openTabFragment() {
        switch (mTab) {
        case 1:
            mCurrent = new StartModeFragment(this);
            break;

        case 2:
            mCurrent = new CourseFragment(this);
            break;

        default:
            mCurrent = new LineStartFragment(this);
            break;
        }
        if (mCurrent != null) {
            openFragment();
        }
    }
}
