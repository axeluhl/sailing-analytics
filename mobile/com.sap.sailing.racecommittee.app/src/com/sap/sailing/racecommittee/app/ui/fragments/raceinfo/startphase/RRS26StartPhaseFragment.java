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
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure2;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.RRS26StartPhaseEventListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceChooseStartModeDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public class RRS26StartPhaseFragment extends RaceFragment implements RRS26StartPhaseEventListener {

    private RaceInfoListener infoListener;

    TextView raceCountdown;
    TextView nextFlagCountdown;
    TextView startModeButtonLabel;
    ImageButton startModeButton;
    ImageButton abortingFlagButton;
    ImageView classFlagUp;
    ImageView startModeFlagUp;
    ImageView classFlagDown;
    ImageView startModeFlagDown;
    Button resetTimeButton;

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
        return inflater.inflate(R.layout.race_startphase_rrs26_view, container, false);
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

        startModeButton = (ImageButton) getView().findViewById(R.id.startModeButton);
        startModeButtonLabel = (TextView) getView().findViewById(R.id.startModeButtonLabel);
        startModeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showStartModeDialog();
            }
        });

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
    }

    @Override
    public void onStart() {
        super.onStart();

        setupUi();

        RRS26RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
        procedure.addChangedListener(changeListener);
        ExLog.i(RRS26StartPhaseFragment.class.getName(), String.format("Fragment %s is now shown", RRS26StartPhaseFragment.class.getName()));
    }

    @Override
    public void onStop() {
        super.onStop();
        RRS26RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
        procedure.removeChangedListener(changeListener);
    }

    @Override
    public void notifyTick() {
        TimePoint startTime = getRace().getState().getStartTime();
        if (startTime != null) {
            setCountdownLabels(TimeUtils.timeUntil(startTime));
        }
    }

    private void setupUi() {
        setStartModeFlags(getRaceState().<RRS26RacingProcedure>getTypedRacingProcedure().getStartModeFlag());
        /*StartModeChoosableStartProcedure startProcedure = (StartModeChoosableStartProcedure) this.getRace().getState().getStartProcedure();
        this.onStartModeFlagChosen(startProcedure.getCurrentStartModeFlag());
        LastFlagsFinder lastFlagFinder = new LastFlagsFinder(this.getRace().getRaceLog());
        RaceLogFlagEvent lastFlagEvent = LastFlagsFinder.getMostRecent(lastFlagFinder.analyze());
        if(lastFlagEvent != null){
            if(lastFlagEvent.getUpperFlag().equals(Flags.CLASS) && lastFlagEvent.isDisplayed()){
                this.onClassUp();
            } else if(lastFlagEvent.getUpperFlag().equals(Flags.PAPA)){
                if(lastFlagEvent.isDisplayed()){
                    onStartModeUp(lastFlagEvent.getUpperFlag());
                } else {
                    onStartModeDown(lastFlagEvent.getUpperFlag());
                }
            } else if(lastFlagEvent.getUpperFlag().equals(Flags.ZULU)){
                if(lastFlagEvent.isDisplayed()){
                    onStartModeUp(lastFlagEvent.getUpperFlag());
                } else {
                    onStartModeDown(lastFlagEvent.getUpperFlag());
                }
            } else if(lastFlagEvent.getUpperFlag().equals(Flags.INDIA)){
                if(lastFlagEvent.isDisplayed()){
                    onStartModeUp(lastFlagEvent.getUpperFlag());
                } else {
                    onStartModeDown(lastFlagEvent.getUpperFlag());
                }
            } else if(lastFlagEvent.getUpperFlag().equals(Flags.BLACK)){
                if(lastFlagEvent.isDisplayed()){
                    onStartModeUp(lastFlagEvent.getUpperFlag());
                } else {
                    onStartModeDown(lastFlagEvent.getUpperFlag());
                }
            } else if(lastFlagEvent.getUpperFlag().equals(Flags.CLASS) && !lastFlagEvent.isDisplayed()){
                onClassDown();
            } 
            
        }*/
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
        //TODO This method needs to be reviewed and changed. Why List<Object>? Isn't there any more concrete return value type than Object?
        //Furthermore the Flag name is not shown correctly
        Pair<String, List<Object>> countdownStringPackage = getRace().getState().getStartProcedure()
                .getNextFlagCountdownUiLabel(getActivity(), millisecondsTillStart);
        CharSequence countdownTime = TimeUtils
                .prettyString(((Number) countdownStringPackage.getB().get(0)).longValue());
        String countDownMetaInfo = (String) countdownStringPackage.getB().get(1);
        nextFlagCountdown.setText(String.format(countdownStringPackage.getA(), countdownTime, countDownMetaInfo));
    }

    protected void showAPModeDialog() {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new AbortModeSelectionDialog();

        Bundle args = getRecentArguments();
        args.putString(AppConstants.FLAG_KEY, Flags.AP.name());
        fragment.setArguments(args);

        fragment.show(fragmentManager, "dialogAPMode");
    }

    protected void showStartModeDialog() {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new RaceChooseStartModeDialog();
        
        Bundle args = getRecentArguments();
        fragment.setArguments(args);
        
        fragment.show(fragmentManager, "dialogStartMode");

    }

    @Override
    public void onClassUp() {
        classFlagUp.setVisibility(View.VISIBLE);
        startModeFlagUp.setVisibility(View.GONE);
        classFlagDown.setVisibility(View.GONE);
        startModeFlagDown.setVisibility(View.VISIBLE);

    }

    @Override
    public void onStartModeUp(Flags startModeFlag) {
        hideStartModeButton();
        setStartModeFlags(startModeFlag);
        classFlagUp.setVisibility(View.VISIBLE);
        startModeFlagUp.setVisibility(View.VISIBLE);
        classFlagDown.setVisibility(View.GONE);
        startModeFlagDown.setVisibility(View.GONE);
    }

    @Override
    public void onStartModeDown(Flags startModeFlag) {
        hideStartModeButton();
        setStartModeFlags(startModeFlag);
        classFlagUp.setVisibility(View.VISIBLE);
        startModeFlagUp.setVisibility(View.GONE);
        classFlagDown.setVisibility(View.GONE);
        startModeFlagDown.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClassDown() {
        hideStartModeButton();
        classFlagUp.setVisibility(View.GONE);
        startModeFlagUp.setVisibility(View.GONE);
        classFlagDown.setVisibility(View.VISIBLE);
        startModeFlagDown.setVisibility(View.VISIBLE);
    }
    
    private void hideStartModeButton() {
        startModeButton.setVisibility(View.GONE);
        startModeButtonLabel.setVisibility(View.GONE);
    }

    private void setStartModeFlags(Flags startModeFlag) {
        if(startModeFlag.equals(Flags.PAPA)){
            startModeFlagDown.setImageResource(R.drawable.papa_flag);
            startModeFlagUp.setImageResource(R.drawable.papa_flag);
        } else if(startModeFlag.equals(Flags.ZULU)){
            startModeFlagDown.setImageResource(R.drawable.zulu_flag);
            startModeFlagUp.setImageResource(R.drawable.zulu_flag);
        } else if(startModeFlag.equals(Flags.INDIA)){
            startModeFlagDown.setImageResource(R.drawable.india_flag);
            startModeFlagUp.setImageResource(R.drawable.india_flag);
        } else if(startModeFlag.equals(Flags.BLACK)){
            startModeFlagDown.setImageResource(R.drawable.black_flag);
            startModeFlagUp.setImageResource(R.drawable.black_flag);
        }
    }

    @Override
    public void onStartModeFlagChosen(Flags startModeFlag) {
        setStartModeFlags(startModeFlag);
    }
    
    private RRS26ChangedListener changeListener = new RRS26ChangedListener() {

        @Override
        public void onActiveFlagsChanged(RacingProcedure2 racingProcedure) {
            ExLog.i("WUHU", "Active flags changed!");
        }
        
        @Override
        public void onIndividualRecallDisplayed(RacingProcedure2 racingProcedure) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onIndividualRecallRemoved(RacingProcedure2 racingProcedure) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onStartmodeChanged(RRS26RacingProcedure racingProcedure) {
            setStartModeFlags(racingProcedure.getStartModeFlag());
        }
        
    };
}
