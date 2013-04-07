package com.sap.sailing.racecommittee.app.ui.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.chooser.RaceInfoFragmentChooser;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.CourseDesignDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.SetStartTimeRaceFragment;


public class RaceInfoFragment extends RaceFragment implements RaceStateChangedListener, RaceInfoListener {
    private final static String TAG = RaceInfoFragment.class.getName();

    private RaceInfoFragmentChooser infoFragmentChooser;
    private RaceFragment infoFragment;

    private TextView fleetInfoHeader;
    private TextView raceInfoHeader;
    private TextView courseInfoHeader;

    public RaceInfoFragment() {
        this.infoFragmentChooser = new RaceInfoFragmentChooser();
        this.infoFragment = null;	// will be set later by switchToInfoFragment()
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_info_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.fleetInfoHeader = (TextView) getView().findViewById(R.id.regattaGroupInfoHeader);
        this.raceInfoHeader = (TextView) getView().findViewById(R.id.raceInfoHeader);
        this.courseInfoHeader = (TextView) getView().findViewById(R.id.courseInfoHeader);

        courseInfoHeader.setText(getString(R.string.running_on_unknown));
        fleetInfoHeader.setText(String.format("%s - %s", 
                getRace().getRaceGroup().getName(), 
                getRace().getFleet().getName()));
        raceInfoHeader.setText(String.format("%s", getRace().getName()));

        /// TODO: implement course selection
        courseInfoHeader.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showCourseDesignDialog();
            }
        });

        Button resetButton = ((Button) getView().findViewById(R.id.btnResetRace));
        resetButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                ExLog.i(TAG, "Reset race button pressed");
                getRace().getState().onRaceAborted(MillisecondsTimePoint.now());
            }
        });

        // Initial fragment selection...
        switchToInfoFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        getRace().getState().registerListener(this);
    }

    @Override
    public void onStop() {
        getRace().getState().unregisterListener(this);
        super.onStop();
    }

    public RaceFragment getInfoFragment() {
        return infoFragment;
    }

    protected void switchToInfoFragment() {
        switchToInfoFragment(infoFragmentChooser.choose(getRace()));
    }

    protected void switchToInfoFragment(RaceFragment choosenFragment) {
        ExLog.i(TAG, String.format("Choosed a %s fragment for race %s with status %s", 
                choosenFragment.getClass().getName(), 
                getRace().getId(), 
                getRace().getStatus()));

        this.infoFragment = choosenFragment;
        displayInfoFragment();
    }

    private void displayInfoFragment() {
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in,
                R.animator.slide_out);
        transaction.replace(R.id.infoContainer, infoFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void onRaceStateChanged(RaceState state) {
        switchToInfoFragment();
    }

    public void onResetTime() {
        switchToInfoFragment(SetStartTimeRaceFragment.create(getRace()));
    }

    private void showCourseDesignDialog() {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new CourseDesignDialogFragment();

        Bundle args = getParameterBundle();
        fragment.setArguments(args);

        fragment.show(fragmentManager, "courseDesignDialogFragment");
    }

    @Override
    public void onChangeCourseDesign() {
        courseInfoHeader.setText("Course design is transmitted");
    }

    @Override
    public void onStartTimeChanged(TimePoint startTime) {
        //do nothing (onRaceStateChanged(RaceState) handles state change and fragment switch already
    }

    @Override
    public void onRaceAborted() {
        //do nothing (onRaceStateChanged(RaceState) handles state change and fragment switch already
    }

    @Override
    public void onIndividualRecall(TimePoint eventTime) {
        //do nothing (onRaceStateChanged(RaceState) handles state change and fragment switch already
    }

}
