package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.GateLineOpeningTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.LastFlagsFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.PathfinderFinder;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.GateStartRunningRaceEventListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortTypeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceChooseGateLineOpeningTimeDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceChoosePathFinderDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceFinishingTimeDialog;

public class GateStartRunningRaceFragment extends RaceFragment implements GateStartRunningRaceEventListener {

    private TextView countUpTextView;
    private ImageView displayedFlag;
    private ImageView flagToBeDisplayed;
    ImageButton abortingFlagButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gate_start_race_running_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        countUpTextView = (TextView) getView().findViewById(R.id.raceCountUp);

        ImageButton blueFlagButton = (ImageButton) getView().findViewById(R.id.blueFlagButton);
        blueFlagButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDisplayBlueFlagDialog();
            }
        });

        abortingFlagButton = (ImageButton) getView().findViewById(R.id.abortingFlagButton);
        abortingFlagButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showChooseAPNovemberDialog();
            }
        });

        ImageButton generalRecallFlagButton = (ImageButton) getView().findViewById(R.id.firstSubstituteButton);
        generalRecallFlagButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDisplayGeneralRecallDialog();
            }
        });

        displayedFlag = (ImageView) getView().findViewById(R.id.currentlyDisplayedFlag);
        flagToBeDisplayed = (ImageView) getView().findViewById(R.id.flagToBeDisplayed);

        getRace().getState().getStartProcedure().setRunningRaceEventListener(this);

        PathfinderFinder pathFinderFinder = new PathfinderFinder(this.getRace().getRaceLog());
        if (pathFinderFinder.analyze()==null) {
            showPathFinderDialog();
        }
        GateLineOpeningTimeFinder gateLineOpeningTimeFinder = new GateLineOpeningTimeFinder(this.getRace().getRaceLog());
        if (gateLineOpeningTimeFinder.analyze()==null) {
            showGateLineOpeningTimeDialog();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        setupUi();
        
        ExLog.i(GateStartRunningRaceFragment.class.getName(),
                String.format("Fragment %s is now shown", GateStartRunningRaceFragment.class.getName()));
    }

    @Override
    public void notifyTick() {
        if (getRace().getState().getStartTime() == null)
            return;

        long millisSinceStart = System.currentTimeMillis() - getRace().getState().getStartTime().asMillis();
        setStarttimeCountupLabel(millisSinceStart);
    }

    private void setupUi() {
        boolean golfFlagTakenDown = false;
        LastFlagsFinder lastFlagFinder = new LastFlagsFinder(getRace().getRaceLog());
        RaceLogFlagEvent lastFlag = LastFlagsFinder.getMostRecent(lastFlagFinder.analyze());
        if (lastFlag != null) {
            golfFlagTakenDown = lastFlag.getUpperFlag().equals(Flags.GOLF) && !lastFlag.isDisplayed();
        }
        displayGolfFlag(!golfFlagTakenDown);
    }

    private void displayGolfFlag(boolean golfFlagDisplayed) {
        if (golfFlagDisplayed) {
            displayedFlag.setVisibility(View.VISIBLE);
            flagToBeDisplayed.setVisibility(View.GONE);
        } else {
            displayedFlag.setVisibility(View.GONE);
            flagToBeDisplayed.setVisibility(View.VISIBLE);
        }

    }

    private void showDisplayGeneralRecallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getResources().getString(R.string.confirmation_first_substitute_display))
                .setCancelable(true)
                .setPositiveButton(getActivity().getResources().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ExLog.i(ExLog.RACE_RUNNING_GENERAL_RECALL_YES, getRace().getId().toString(),
                                        getActivity());
                                getRace().getState().getStartProcedure().setGeneralRecall(MillisecondsTimePoint.now());
                            }
                        })
                .setNegativeButton(getActivity().getResources().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ExLog.i(ExLog.RACE_RUNNING_GENERAL_RECALL_NO, getRace().getId().toString(),
                                        getActivity());
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDisplayBlueFlagDialog() {
      //TODO handle xray still up
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new RaceFinishingTimeDialog();
        
        Bundle args = getRecentArguments();
        fragment.setArguments(args);
        
        fragment.show(fragmentManager, "dialogFinishingTime");
    }

    private void setStarttimeCountupLabel(long millisecondsSinceStart) {
        countUpTextView.setText(String.format(getString(R.string.race_running_since_template),
                prettyTimeString(millisecondsSinceStart), getRace().getName()));
    }

    protected CharSequence prettyTimeString(long time) {
        int secondsStart = (int) (time / 1000);
        int hours = secondsStart / 3600;
        int minutes = (secondsStart % 3600) / 60;
        int seconds = (secondsStart % 60);
        String timePattern = "%s:%s:%s";
        String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
        String minutesString = minutes < 10 ? "0" + minutes : "" + minutes;
        String hoursString = hours < 10 ? "0" + hours : "" + hours;
        return String.format(timePattern, hoursString, minutesString, secondsString);
    }

    private void showChooseAPNovemberDialog() {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new AbortTypeSelectionDialog();

        Bundle args = getRecentArguments();
        fragment.setArguments(args);

        fragment.show(fragmentManager, "dialogAPNovemberMode");
    }

    @Override
    public void onGolfDown() {
        displayGolfFlag(false);
    }

    private void showPathFinderDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        RaceDialogFragment fragment = new RaceChoosePathFinderDialog();
        Bundle args = getRecentArguments();
        fragment.setArguments(args);
        fragment.show(fragmentManager, null);
    }

    private void showGateLineOpeningTimeDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        RaceDialogFragment fragment = new RaceChooseGateLineOpeningTimeDialog();
        Bundle args = getRecentArguments();
        fragment.setArguments(args);
        fragment.show(fragmentManager, null);
    }

}
