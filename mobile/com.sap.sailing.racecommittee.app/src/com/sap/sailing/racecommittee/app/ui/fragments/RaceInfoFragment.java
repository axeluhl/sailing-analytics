package com.sap.sailing.racecommittee.app.ui.fragments;

import java.io.Serializable;

import android.app.AlertDialog;
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

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.fragments.chooser.RaceInfoFragmentChooser;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.SetStartTimeRaceFragment;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceInfoFragment extends RaceFragment implements RaceInfoListener {

    private final static String TAG = RaceInfoFragment.class.getName();

    private RaceInfoFragmentChooser infoFragmentChooser;
    private RaceFragment infoFragment;

    private TextView fleetInfoHeader;
    private TextView raceInfoHeader;
    private TextView courseInfoHeader;

    private View resetRaceDialogView;

    public RaceInfoFragment() {
        this.infoFragmentChooser = null;
        this.infoFragment = null; // will be set later by switchToInfoFragment()
    }

    public static RaceInfoFragment newInstance() {
        RaceInfoFragment fragment = new RaceInfoFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_info_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        infoFragmentChooser = RaceInfoFragmentChooser.on(getRaceState().getRacingProcedure().getType());

        fleetInfoHeader = (TextView) getView().findViewById(R.id.regattaGroupInfoHeader);
        raceInfoHeader = (TextView) getView().findViewById(R.id.raceInfoHeader);
        courseInfoHeader = (TextView) getView().findViewById(R.id.courseInfoHeader);

        fleetInfoHeader.setText(String.format("%s - %s", getRace().getRaceGroup().getName(), getRace().getFleet()
                .getName()));
        raceInfoHeader.setText(String.format("%s", getRace().getName()));

        // TODO: catch result WIND_ACTIVITY_REQUEST_CODE ;

        Button resetButton = (Button) getView().findViewById(R.id.btnResetRace);
        resetButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                ExLog.i(getActivity(), TAG, "Reset race button pressed");
                showRaceResetConfirmationDialog();
            }
        });

        // Initial fragment selection...

        switchToInfoFragment();
        updateCourseDesignLabel();
        updateWindLabel();
    }

    public void onResume() {
        super.onResume();
        switchToInfoFragment();
    }

    protected void switchToInfoFragment() {
        RaceFragment newInfoFragment = infoFragmentChooser.choose(getActivity(), getRace());
        if (infoFragment == null || !newInfoFragment.getClass().equals(infoFragment.getClass())) {
            switchToInfoFragment(newInfoFragment);
        }
    }

    protected void switchToInfoFragment(RaceFragment chosenFragment) {
        ExLog.i(getActivity(), TAG, String.format("Switched to %s fragment for race %s with status %s", chosenFragment
                .getClass().getName(), getRace().getId(), getRace().getStatus()));

        this.infoFragment = chosenFragment;
        displayInfoFragment();
    }

    private void displayInfoFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//        transaction.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
        transaction.replace(R.id.infoContainer, infoFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    private void showRaceResetConfirmationDialog() {
        prepareResetRaceView();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(resetRaceDialogView).setTitle(R.string.race_reset_confirmation_title)
                .setIcon(R.drawable.ic_warning_grey600_36dp).setCancelable(true)
                .setPositiveButton(getString(R.string.race_reset_reset_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ExLog.i(getActivity(), LogEvent.RACE_RESET_YES, getRace().getId().toString());
                        ExLog.w(getActivity(), TAG, String.format("Race %s is selected for reset.", getRace().getId()));

                        getRace().getState().setAdvancePass(MillisecondsTimePoint.now());
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ExLog.i(getActivity(), LogEvent.RACE_RESET_NO, getRace().getId().toString());
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        ExLog.i(getActivity(), LogEvent.RACE_RESET_DIALOG_BUTTON, getRace().getId().toString());
        alert.show();
    }

    private void prepareResetRaceView() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resetRaceDialogView = inflater.inflate(R.layout.race_reset_confirmation, null);
        TextView raceInfoView = (TextView) resetRaceDialogView.findViewById(R.id.textRaceResetRaceInfo);

        ManagedRace race = getRace();
        raceInfoView.setText(String.format("%s - %s - %s", race.getRaceGroup().getName(), race.getFleet().getName(),
                race.getRaceName()));
    }

    private void updateCourseDesignLabel() {
        CourseBase courseDesign = getRaceState().getCourseDesign();
        if (courseDesign != null) {
            if (Util.isEmpty(courseDesign.getWaypoints())) {
                String courseName = courseDesign.getName();
                courseInfoHeader.setText(String.format(getString(R.string.running_on_course), courseName));
            } else {
                courseInfoHeader.setText(String.format(getString(R.string.course_design_number_waypoints),
                        Util.size(courseDesign.getWaypoints())));
            }
        } else {
            courseInfoHeader.setText(getString(R.string.no_course_active));
        }
    }

    private void updateWindLabel() {
        Wind wind = getRaceState().getWindFix();
        if (wind != null) {
            TextView windValue = (TextView) getActivity().findViewById(R.id.wind_value);
            if (windValue != null) {
                windValue.setText(String.format(getString(R.string.wind_info), wind.getKnots(), wind.getBearing()
                        .reverse().toString()));
            }
        }
    }

    public void updateArguments(Bundle args) {
        Serializable raceId = args.getSerializable(AppConstants.RACE_ID_KEY);
        managedRace = OnlineDataManager.create(getActivity()).getDataStore().getRace(raceId);
        if (managedRace == null) {
            throw new IllegalStateException(
                    String.format("Unable to obtain ManagedRace from datastore on start of race fragment."));
        }
    }

    @Override
    public void onResetTime() {
        switchToInfoFragment(SetStartTimeRaceFragment.create(getRace()));
    }

}
