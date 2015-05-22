package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finishing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceFinishedTimeDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseRaceInfoRaceFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class BaseFinishingRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType> {   
    
    private TextView startCountUpTextView;
    protected TextView additionalInfoTextView;
    
    private ImageButton abortButton;
    private ImageButton finishedButton;
    private Button resetCourseButton;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_finishing_view, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        startCountUpTextView = (TextView) getView().findViewById(R.id.race_finishing_race_countup);
        additionalInfoTextView = (TextView) getView().findViewById(R.id.race_finishing_additional_info);
        finishedButton = (ImageButton) getView().findViewById(R.id.race_finishing_finished);
        abortButton = (ImageButton) getView().findViewById(R.id.race_finishing_abort);
        resetCourseButton = (Button) getView().findViewById(R.id.race_finishing_reset_course);
        
        finishedButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setFinishedTime();
            }
        });

        abortButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RaceDialogFragment fragment = new AbortModeSelectionDialog();
                Bundle args = getRecentArguments();
                args.putString(AppConstants.FLAG_KEY, Flags.NOVEMBER.name());
                fragment.setArguments(args);
                fragment.show(getFragmentManager(), "dialogNovemberMode");
            }
        });
        
        resetCourseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCourseDesignDialog();
            }
        });
        
    }
    
    @Override
    public void notifyTick() {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint startTime = getRaceState().getStartTime();
        if (startTime != null) {
            long millisecondsSinceStart = now.minus(startTime.asMillis()).asMillis();
            
            startCountUpTextView.setText(String.format(
                    getString(R.string.race_running_since_template),
                    getRace().getName(), TimeUtils.formatDurationSince(millisecondsSinceStart)));
        }
        
        String info = updateAdditionalInfoText();
        if (info != null) {
            additionalInfoTextView.setText(info);
        }
        super.notifyTick();
    }
    
    @Override
    protected void setupUi() {
        // nothing here
    }

    protected String updateAdditionalInfoText() {
        TimePoint finishingStartedAt = getRaceState().getFinishingTime();
        if (finishingStartedAt != null) {
            return String.format(getString(R.string.race_first_finisher), TimeUtils.formatTime(finishingStartedAt));
        }
        return null;
    }

    protected void setFinishedTime() {
        RaceDialogFragment fragment = new RaceFinishedTimeDialog();
        fragment.setArguments(getRecentArguments());
        fragment.show(getFragmentManager(), "dialogFinishedTime");
    }
}
