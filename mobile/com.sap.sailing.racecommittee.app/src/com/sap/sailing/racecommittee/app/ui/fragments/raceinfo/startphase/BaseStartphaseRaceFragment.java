package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceInfoListener;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public abstract class BaseStartphaseRaceFragment extends RaceFragment {

    private RaceInfoListener infoListener;
    private TextView startCountdownTextView;
    private ImageButton abortButton;
    private Button resetTimeButton;
    private TextView nextCountdownTextView;
    
    protected ViewGroup upperFlagsViewGroup;
    protected ViewGroup lowerFlagsViewGroup;
    
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
        View view = inflater.inflate(R.layout.race_startphase_base_view, container, false);
        ViewStub actionsStub = (ViewStub) view.findViewById(R.id.race_startphase_base_actions);
        int actionsLayout = getActionsLayoutId();
        if (actionsLayout != 0) {
            actionsStub.setLayoutResource(actionsLayout);
            actionsStub.inflate();
        }
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        startCountdownTextView = (TextView) getView().findViewById(R.id.race_startphase_base_start_countdown);
        nextCountdownTextView = (TextView) getView().findViewById(R.id.race_startphase_base_next_countdown);
        
        abortButton = (ImageButton) getView().findViewById(R.id.race_startphase_base_abort_button);
        abortButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RaceDialogFragment fragment = new AbortModeSelectionDialog();
                Bundle args = getRecentArguments();
                args.putString(AppConstants.FLAG_KEY, Flags.AP.name());
                fragment.setArguments(args);
                fragment.show(getFragmentManager(), "dialogAPMode");
            }
        });
        
        resetTimeButton = (Button) getView().findViewById(R.id.race_startphase_base_reset_time_button);
        resetTimeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                infoListener.onResetTime();
            }
        });
        
        upperFlagsViewGroup = (ViewGroup) getView().findViewById(R.id.race_startphase_base_up_flags);
        lowerFlagsViewGroup = (ViewGroup) getView().findViewById(R.id.race_startphase_base_down_flags);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        setupUi();
    }
    

    @Override
    public void onStop() {
        
        super.onStop();
    }

    @Override
    public void notifyTick() {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint startTime = getRaceState().getStartTime();
        if (startTime != null) {
            long millisecondsTillStart = startTime.minus(now.asMillis()).asMillis();
            
            startCountdownTextView.setText(String.format(
                    getString(R.string.race_startphase_countdown_start),
                    TimeUtils.prettyString(millisecondsTillStart), getRace().getName()));
            
            FlagPoleState flagState = getRaceState().getRacingProcedure().getActiveFlags(startTime, now);
            if (flagState.hasNextState()) {
                // TODO: get changing flag and display on nextCountdownTextView
                long millisecondsTillChange = flagState.getNextStateValidFrom().minus(now.asMillis()).asMillis();
                nextCountdownTextView.setText(String.format("%s until next flag...", TimeUtils.prettyString(millisecondsTillChange)));
            }
        }
        
    }
    
    protected ImageView createFlagImageView(int flagDrawableId) {
        ImageView flagView = new ImageView(getActivity());
        flagView.setLayoutParams(new LinearLayout.LayoutParams(200, 130));
        flagView.setImageResource(flagDrawableId);
        return flagView;
    }

    protected int getActionsLayoutId() {
        return 0;
    }
    
    protected abstract void setupUi();

}
