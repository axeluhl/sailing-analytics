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

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.EssStartPhaseEventListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public class EssStartPhaseFragment extends RaceFragment implements EssStartPhaseEventListener {

    private RaceInfoListener infoListener;
    
    TextView raceCountdown;
    TextView nextFlagCountdown;
    ImageButton abortingFlagButton;
    ImageView displayedFlag;
    ImageView nextToBeDisplayedFlag;
    Button resetTimeButton;

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
        return inflater.inflate(R.layout.race_startphase_ess_view, container, false);
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

        Button resetTimeButton = (Button) getView().findViewById(R.id.resetTimeButton);
        resetTimeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                infoListener.onResetTime();
            }
        });
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        getRace().getState().getStartProcedure().setEssStartPhaseEventListener(this);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        getRace().getState().getStartProcedure().setEssStartPhaseEventListener(null);
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

        Bundle args = getParameterBundle();
        args.putString(AppConstants.FLAG_KEY, Flags.AP.name());
        fragment.setArguments(args);

        fragment.show(fragmentManager, "dialogAPMode");
    }

    @Override
    public void onAPDown() {
        displayedFlag.setVisibility(View.INVISIBLE);
        
        resetTimeButton.setEnabled(false);
        resetTimeButton.setVisibility(View.GONE);
    }

    @Override
    public void onEssThreeUp() {
        displayedFlag.setVisibility(View.VISIBLE);
        displayedFlag.setImageResource(R.drawable.three_min_flag);
        
        nextToBeDisplayedFlag.setImageResource(R.drawable.two_min_flag);
    }

    @Override
    public void onEssTwoUp() {
        displayedFlag.setImageResource(R.drawable.two_min_flag);
        
        nextToBeDisplayedFlag.setImageResource(R.drawable.one_min_flag);
    }

    @Override
    public void onEssOneUp() {
        displayedFlag.setImageResource(R.drawable.one_min_flag);
        
        nextToBeDisplayedFlag.setVisibility(View.INVISIBLE);
    }
    
    

}
