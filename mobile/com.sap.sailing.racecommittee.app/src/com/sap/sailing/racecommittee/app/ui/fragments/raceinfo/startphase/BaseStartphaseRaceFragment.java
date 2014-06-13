package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseRaceInfoRaceFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagPoleStateRenderer;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public abstract class BaseStartphaseRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType> {
    
    private TextView startCountdownTextView;
    private ImageButton abortButton;
    private Button resetTimeButton;
    protected Button raceStartIn4Minutes;
    protected Button raceStartIn2Minutes;
    protected Button raceResetCourse;
    private TextView nextCountdownTextView;
    
    private FlagPoleStateRenderer flagRenderer;
    
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
        
        raceStartIn4Minutes = (Button) getView().findViewById(R.id.raceStartIn4Minutes);
        raceStartIn4Minutes.setVisibility(View.INVISIBLE);
        
        raceStartIn2Minutes = (Button) getView().findViewById(R.id.raceStartIn1Minute);
        raceStartIn2Minutes.setVisibility(View.INVISIBLE);
        
        raceResetCourse = (Button) getView().findViewById(R.id.raceResetCourse);
        raceResetCourse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View paramView) {
                showCourseDesignDialog();
            }
        });

        flagRenderer = new FlagPoleStateRenderer(getActivity(), getRace(),
                (LinearLayout) getView().findViewById(R.id.race_flag_space_up_flags), 
                (LinearLayout) getView().findViewById(R.id.race_flag_space_down_flags));
    }

    @Override
    public void notifyTick() {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint startTime = getRaceState().getStartTime();
        if (startTime != null) {
            long millisecondsTillStart = startTime.minus(now.asMillis()).asMillis();
            
            startCountdownTextView.setText(String.format(
                    getString(R.string.race_startphase_countdown_start),
                    TimeUtils.formatDurationUntil(millisecondsTillStart), getRace().getName()));
            updateFlagChangesCountdown(nextCountdownTextView);
            
        }
        super.notifyTick();
    }

    protected int getActionsLayoutId() {
        return 0;
    }
    
    @Override
    protected void setupUi() {
        TimePoint startTime = getRaceState().getStartTime();
        if (startTime != null) {
            flagRenderer.render(getRacingProcedure().getActiveFlags(startTime, MillisecondsTimePoint.now()));
        }
    }

}
