package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

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

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.GateStartPhaseEventListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.GateStartUiListener;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceChooseLineOpeningTimeDialog;
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
    ImageView displayedFlag;
    ImageView nextToBeDisplayedFlag;
    Button resetTimeButton;
    Button pathFinderButton;
    Button lineOpeningTimeButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof RaceInfoListener) {
            this.infoListener = (RaceInfoListener) activity;
        } else {
            throw new UnsupportedOperationException(String.format(
                    "%s must implement %s", 
                    activity, 
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
        displayedFlag = (ImageView) getView().findViewById(R.id.currentlyDisplayedFlag);
        nextToBeDisplayedFlag = (ImageView) getView().findViewById(R.id.nextFlagToBeDisplayed);

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
        if(getRace().getState().getPathfinder()!=null){
            pathfinderLabel.setText(getRace().getState().getPathfinder());
        }
        lineOpeningTimeLabel = (TextView) getView().findViewById(R.id.lineOpeningTimeLabel);
        if(getRace().getState().getGateLineOpeningTime()!=null){
            lineOpeningTimeLabel.setText(String.valueOf(getRace().getState().getGateLineOpeningTime()/(60 * 1000))+" minutes");
        }
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
        RaceDialogFragment fragment = new RaceChooseLineOpeningTimeDialog();
        Bundle args = getRecentArguments();
        fragment.setArguments(args);
        fragment.show(fragmentManager, null);
    }

    private void setupUi() {
        RaceLog log = getRace().getState().getRaceLog();
        log.lockForRead();
        try {
            RaceLogEvent lastEvent = log.getLastFixAtOrBefore(MillisecondsTimePoint.now());
            if (lastEvent instanceof RaceLogFlagEvent) {
                RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) lastEvent;
                Flags flag = flagEvent.getUpperFlag();
                
                if (flagEvent.isDisplayed() && flag.equals(Flags.CLASS) && flagEvent.getLowerFlag().equals(Flags.GOLF)) {
                    onClassOverGolfUp();
                } else if (flagEvent.isDisplayed() && flag.equals(Flags.PAPA)) {
                    onPapaUp();
                } else if (!flagEvent.isDisplayed() && flag.equals(Flags.PAPA)) {
                    onPapaDown(); 
                }
            } else
                displayedFlag.setVisibility(View.INVISIBLE);
        } finally {
            log.unlockAfterRead();
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        getRace().getState().getStartProcedure().setStartPhaseEventListener(this);
        ExLog.w(GateStartPhaseFragment.class.getName(), String.format("Fragment %s is now shown", GateStartPhaseFragment.class.getName()));
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
        raceCountdown.setText(String.format(
                getString(R.string.race_startphase_countdown_start),
                TimeUtils.prettyString(millisecondsTillStart), getRace().getName()));
    }

    private void setNextFlagCountdownLabel(long millisecondsTillStart) {
        Pair<String, Long> countDownPair = getRace().getState().getStartProcedure().getNextFlagCountdownUiLabel(getActivity(), millisecondsTillStart);
        nextFlagCountdown.setText(String.format(countDownPair.getA(),
                TimeUtils.prettyString(countDownPair.getB().longValue())));
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
    public void onClassOverGolfUp() {
        displayedFlag.setVisibility(View.VISIBLE);
        
        resetTimeButton.setEnabled(false);
        resetTimeButton.setVisibility(View.GONE);
        nextToBeDisplayedFlag.setImageResource(R.drawable.papa_flag);
    }

    @Override
    public void onPapaUp() {
        nextToBeDisplayedFlag.setVisibility(View.INVISIBLE);
        displayedFlag.setImageResource(R.drawable.papa_flag);
    }

    @Override
    public void onPapaDown() {
        displayedFlag.setImageResource(R.drawable.fivehundred_five_over_golf_flag);
    }

    @Override
    public void updatePathfinderLabel() {
        if (getRace().getState().getPathfinder() != null) {
            pathfinderLabel.setText(getRace().getState().getPathfinder());
        }
    }

    @Override
    public void updateGateLineOpeningTimeLabel() {
        if (getRace().getState().getGateLineOpeningTime() != null) {
            lineOpeningTimeLabel.setText(String.valueOf(getRace().getState().getGateLineOpeningTime()/(60 * 1000))+" minutes");
        }
    }
}
