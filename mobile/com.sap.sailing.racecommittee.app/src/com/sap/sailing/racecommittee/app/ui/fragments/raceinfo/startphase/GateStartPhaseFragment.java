package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.LastFlagsFinder;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.GateStartPhaseEventListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.GateStartProcedure;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.GateStartUiListener;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceChooseGateLineOpeningTimeDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceChoosePathFinderDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public class GateStartPhaseFragment extends RaceFragment implements GateStartPhaseEventListener, GateStartUiListener {

    private RaceInfoListener infoListener;

    TextView raceCountdown;
    TextView nextFlagCountdown;
    TextView pathfinderLabel;
    TextView lineOpeningTimeLabel;
    ImageButton abortingFlagButton;
    ImageView classFlagUp;
    ImageView startModeFlagUp;
    ImageView classFlagDown;
    ImageView startModeFlagDown;
    Button resetTimeButton;
    Button pathFinderButton;
    Button lineOpeningTimeButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof RaceInfoListener) {
            this.infoListener = (RaceInfoListener) activity;
        } else {
            throw new UnsupportedOperationException(String.format("%s must implement %s", activity,
                    RaceInfoListener.class.getName()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_startphase_gate_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        raceCountdown = (TextView) getView().findViewById(R.id.raceCountdown);
        nextFlagCountdown = (TextView) getView().findViewById(R.id.nextFlagCountdown);
        classFlagUp = (ImageView) getView().findViewById(R.id.classFlagUp);
        startModeFlagUp = (ImageView) getView().findViewById(R.id.startModeFlagUp);
        classFlagDown = (ImageView) getView().findViewById(R.id.classFlagDown);
        startModeFlagDown = (ImageView) getView().findViewById(R.id.startModeFlagDown);

        ExLog.i("STARTPHASE", "" + getRace().getId() + " " + getRace().getStatus().toString());

        abortingFlagButton = (ImageButton) getView().findViewById(R.id.abortingFlagButton);
        abortingFlagButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showAPModeDialog();
            }
        });

        resetTimeButton = (Button) getView().findViewById(R.id.resetTimeButton);
        resetTimeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                infoListener.onResetTime();
            }
        });
        pathFinderButton = (Button) getView().findViewById(R.id.pathFinderButton);
        lineOpeningTimeButton = (Button) getView().findViewById(R.id.lineOpeningTimeButton);
        pathfinderLabel = (TextView) getView().findViewById(R.id.pathfinderLabel);
        lineOpeningTimeLabel = (TextView) getView().findViewById(R.id.lineOpeningTimeLabel);

        pathFinderButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                showPathFinderDialog();
            }
        });

        lineOpeningTimeButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                showLineOpeningTimeDialog();
            }
        });

        setupUi();
    }

    private void showPathFinderDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        RaceDialogFragment fragment = new RaceChoosePathFinderDialog();
        Bundle args = getRecentArguments();
        fragment.setArguments(args);
        fragment.show(fragmentManager, null);
    }

    private void showLineOpeningTimeDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        RaceDialogFragment fragment = new RaceChooseGateLineOpeningTimeDialog();
        Bundle args = getRecentArguments();
        fragment.setArguments(args);
        fragment.show(fragmentManager, null);
    }

    private void setupUi() {
        if (getRace().getState().getStartProcedure() instanceof GateStartProcedure) {
            GateStartProcedure gateStartProcedure = ((GateStartProcedure) getRace().getState().getStartProcedure());
            if (gateStartProcedure.getPathfinder() != null) {
                pathfinderLabel.setText(gateStartProcedure.getPathfinder());
            }
            if (gateStartProcedure.getGateLineOpeningTime() != null) {
                lineOpeningTimeLabel.setText(String.valueOf(gateStartProcedure.getGateLineOpeningTime() / (60 * 1000))
                        + " minutes");
            }
        }
        LastFlagsFinder lastFlagFinder = new LastFlagsFinder(this.getRace().getRaceLog());
        RaceLogFlagEvent lastFlagEvent = LastFlagsFinder.getMostRecent(lastFlagFinder.analyze());
        if (lastFlagEvent != null) {
            if (lastFlagEvent.getUpperFlag().equals(Flags.CLASS) && lastFlagEvent.getLowerFlag().equals(Flags.GOLF)
                    && lastFlagEvent.isDisplayed()) {
                this.onClassOverGolfUp();
            } else if (lastFlagEvent.getUpperFlag().equals(Flags.PAPA)) {
                if (lastFlagEvent.isDisplayed()) {
                    onPapaUp();
                } else {
                    onPapaDown();
                }
            } else if (lastFlagEvent.getUpperFlag().equals(Flags.CLASS) && !lastFlagEvent.isDisplayed()) {
                onClassOverGolfDown();
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getRace().getState().getStartProcedure().setStartPhaseEventListener(this);
        ExLog.w(GateStartPhaseFragment.class.getName(),
                String.format("Fragment %s is now shown", GateStartPhaseFragment.class.getName()));
    }

    @Override
    public void onStop() {
        super.onStop();

        getRace().getState().getStartProcedure().setStartPhaseEventListener(null);
    }

    @Override
    public void notifyTick() {
        TimePoint startTime = getRace().getState().getStartTime();
        if (startTime != null) {
            setCountdownLabels(TimeUtils.timeUntil(startTime));
        }
    }

    private void setCountdownLabels(long millisecondsTillStart) {
        setStarttimeCountdownLabel(millisecondsTillStart);
        setNextFlagCountdownLabel(millisecondsTillStart);
    }

    private void setStarttimeCountdownLabel(long millisecondsTillStart) {
        raceCountdown.setText(String.format(getString(R.string.race_startphase_countdown_start),
                TimeUtils.prettyString(millisecondsTillStart), getRace().getName()));
    }

    private void setNextFlagCountdownLabel(long millisecondsTillStart) {
        Pair<String, List<Object>> countdownStringPackage = getRace().getState().getStartProcedure()
                .getNextFlagCountdownUiLabel(getActivity(), millisecondsTillStart);
        nextFlagCountdown.setText(String.format(countdownStringPackage.getA(),
                TimeUtils.prettyString(((Number) countdownStringPackage.getB().get(0)).longValue())));
    }

    protected void showAPModeDialog() {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new AbortModeSelectionDialog();

        Bundle args = getRecentArguments();
        args.putString(AppConstants.FLAG_KEY, Flags.AP.name());
        fragment.setArguments(args);

        fragment.show(fragmentManager, "dialogAPMode");
    }

    @Override
    public void updatePathfinderLabel() {
        if (getRace().getState().getStartProcedure() instanceof GateStartProcedure) {
            GateStartProcedure gateStartProcedure = ((GateStartProcedure) getRace().getState().getStartProcedure());
            if (gateStartProcedure.getPathfinder() != null) {
                pathfinderLabel.setText(gateStartProcedure.getPathfinder());
            }
        }
    }

    @Override
    public void updateGateLineOpeningTimeLabel() {
        if (getRace().getState().getStartProcedure() instanceof GateStartProcedure) {
            GateStartProcedure gateStartProcedure = ((GateStartProcedure) getRace().getState().getStartProcedure());
            if (gateStartProcedure.getGateLineOpeningTime() != null) {
                lineOpeningTimeLabel.setText(String.valueOf(gateStartProcedure.getGateLineOpeningTime() / (60 * 1000))
                        + " minutes");
            }
        }
    }

    @Override
    public void onPathFinderSet() {
        if (getRace().getState().getStartProcedure() instanceof GateStartProcedure) {
            GateStartProcedure gateStartProcedure = ((GateStartProcedure) getRace().getState().getStartProcedure());
            if (gateStartProcedure.getPathfinder() != null) {
                pathfinderLabel.setText(gateStartProcedure.getPathfinder());
            }
        }
    }

    @Override
    public void onGateLineOpeningTimeSet() {
        if (getRace().getState().getStartProcedure() instanceof GateStartProcedure) {
            GateStartProcedure gateStartProcedure = ((GateStartProcedure) getRace().getState().getStartProcedure());
            if (gateStartProcedure.getGateLineOpeningTime() != null) {
                lineOpeningTimeLabel.setText(String.valueOf(gateStartProcedure.getGateLineOpeningTime() / (60 * 1000))
                        + " minutes");
            }
        }
    }

    @Override
    public void onClassOverGolfUp() {
        classFlagUp.setVisibility(View.VISIBLE);
        startModeFlagUp.setVisibility(View.GONE);
        classFlagDown.setVisibility(View.GONE);
        startModeFlagDown.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPapaUp() {
        classFlagUp.setVisibility(View.VISIBLE);
        startModeFlagUp.setVisibility(View.VISIBLE);
        classFlagDown.setVisibility(View.GONE);
        startModeFlagDown.setVisibility(View.GONE);
    }

    @Override
    public void onPapaDown() {
        classFlagUp.setVisibility(View.VISIBLE);
        startModeFlagUp.setVisibility(View.GONE);
        classFlagDown.setVisibility(View.GONE);
        startModeFlagDown.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClassOverGolfDown() {
        classFlagUp.setVisibility(View.GONE);
        startModeFlagUp.setVisibility(View.GONE);
        classFlagDown.setVisibility(View.VISIBLE);
        startModeFlagDown.setVisibility(View.VISIBLE);
    }
}
