package com.sap.sailing.racecommittee.app.ui.fragments;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.chooser.RaceInfoFragmentChooser;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.CourseDesignDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.IndividualRecallUiListener;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.GateStartUiListener;
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
    
    private View resetRaceDialogView;

    public RaceInfoFragment() {
        this.infoFragmentChooser = null;
        this.infoFragment = null;	// will be set later by switchToInfoFragment()
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_info_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // decide on start procedure...
        this.infoFragmentChooser = new RaceInfoFragmentChooser();

        this.fleetInfoHeader = (TextView) getView().findViewById(R.id.regattaGroupInfoHeader);
        this.raceInfoHeader = (TextView) getView().findViewById(R.id.raceInfoHeader);
        this.courseInfoHeader = (TextView) getView().findViewById(R.id.courseInfoHeader);

        courseInfoHeader.setText(getString(R.string.running_on_unknown));
        fleetInfoHeader.setText(String.format("%s - %s", 
                getRace().getRaceGroup().getName(), 
                getRace().getFleet().getName()));
        raceInfoHeader.setText(String.format("%s", getRace().getName()));

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
                showRaceResetConfirmationDialog();
            }
        });

        // Initial fragment selection...
        switchToInfoFragment();
        
        updateCourseDesignLabel();
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
        RaceFragment newInfoFragment = infoFragmentChooser.choose(getRace());
        if (infoFragment == null || !newInfoFragment.getClass().equals(infoFragment.getClass())) {
            switchToInfoFragment(newInfoFragment);            
        }
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
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
        transaction.replace(R.id.infoContainer, infoFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void onRaceStateChanged(RaceState state) {
        updateCourseDesignLabel();
        switchToInfoFragment();
    }

    public void onResetTime() {
        switchToInfoFragment(SetStartTimeRaceFragment.create(getRace()));
    }

    private void showCourseDesignDialog() {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new CourseDesignDialogFragment();

        Bundle args = getRecentArguments();
        fragment.setArguments(args);

        fragment.show(fragmentManager, "courseDesignDialogFragment");
    }

    @Override
    public void onChangeCourseDesign() {
        updateCourseDesignLabel();
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
    public void onIndividualRecallDisplayed(TimePoint individualRecallRemovalFireTimePoint) {
        if (infoFragment instanceof IndividualRecallUiListener) {
            ((IndividualRecallUiListener) infoFragment).displayIndividualRecallFlag();
        }
    }

    private void showRaceResetConfirmationDialog() {
        prepareResetRaceView();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(resetRaceDialogView)
        .setTitle(R.string.race_reset_confirmation_title)
        .setIcon(R.drawable.ic_dialog_alert_holo_light)
        .setCancelable(true)
        .setPositiveButton("Reset anyway",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.RACE_RESET_YES, getRace().getId().toString(), getActivity());
                ExLog.w(TAG, String.format("Race %s is selected for reset.", getRace().getId()));

                getRace().getState().onRaceAborted(MillisecondsTimePoint.now());
            }
        })
        .setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.RACE_RESET_NO, getRace().getId().toString(), getActivity());
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        ExLog.i(ExLog.RACE_RESET_DIALOG_BUTTON, getRace().getId().toString(), getActivity());
        alert.show();
    }

    private void prepareResetRaceView() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resetRaceDialogView = inflater.inflate(R.layout.race_reset_confirmation, null);
        TextView raceInfoView = (TextView) resetRaceDialogView.findViewById(R.id.textRaceResetRaceInfo);

        ManagedRace race = getRace();
        raceInfoView.setText(String.format("%s - %s - %s", race.getRaceGroup().getName(), race.getFleet().getName(), race.getRaceName()));
    }

    public void updateCourseDesignLabel() {
        if (getRace().getState().getCourseDesign() != null) {

            CourseBase courseDesign = getRace().getState().getCourseDesign();
            if (Util.isEmpty(courseDesign.getWaypoints())) {
                courseInfoHeader.setText(getString(R.string.running_on_unknown));
            } else {
                courseInfoHeader.setText(String.format(
                        getString(R.string.course_design_number_waypoints),
                        Util.size(courseDesign.getWaypoints())));
            }
        } else {
            courseInfoHeader.setText(getString(R.string.running_on_unknown));
        }
    }

    @Override
    public void notifyTick() {
        //do nothing
    }

    @Override
    public void onIndividualRecallRemoval() {
        if (infoFragment instanceof IndividualRecallUiListener) {
            ((IndividualRecallUiListener) infoFragment).removeIndividualRecallFlag();
        }
    }

    @Override
    public void onAutomaticRaceEnd(TimePoint automaticRaceEnd) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPathfinderSelected() {
        if (infoFragment instanceof GateStartUiListener) {
            ((GateStartUiListener) infoFragment).updatePathfinderLabel();
        }
    }

    @Override
    public void onGateLineOpeningTimeChanged() {
        if (infoFragment instanceof GateStartUiListener) {
            ((GateStartUiListener) infoFragment).updateGateLineOpeningTimeLabel();
        }
    }

    @Override
    public void onGateLineOpeningTimeTrigger(TimePoint gateCloseTimePoint) {
        // TODO Auto-generated method stub
        
    }

}
