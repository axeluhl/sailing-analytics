package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

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

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortTypeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceFinishingTimeDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseRaceInfoRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running.ConfirmDialog.ConfirmRecallListener;
import com.sap.sailing.racecommittee.app.ui.utils.FlagPoleStateRenderer;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public abstract class BaseRunningRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType> {

    private ImageButton abortButton;
    private ImageButton finishingButton;
    private ImageButton generalRecallButton;
    private TextView startCountUpTextView;
    private TextView nextCountdownTextView;
    private Button resetCourseButton;

    protected ImageButton individualRecallButton;
    protected TextView individualRecallLabel;
    
    private FlagPoleStateRenderer flagRenderer;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_running_base_view, container, false);
        ViewStub actionsStub = (ViewStub) view.findViewById(R.id.race_running_base_actions);
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
        
        startCountUpTextView = (TextView) getView().findViewById(R.id.race_running_base_start_countup);
        nextCountdownTextView = (TextView) getView().findViewById(R.id.race_running_base_next_countdown);
        abortButton = (ImageButton) getView().findViewById(R.id.race_running_base_abort);
        finishingButton = (ImageButton) getView().findViewById(R.id.race_running_base_finishing);
        generalRecallButton = (ImageButton) getView().findViewById(R.id.race_running_base_general_recall);
        individualRecallButton = (ImageButton) getView().findViewById(R.id.race_running_base_individual_recall);
        individualRecallLabel = (TextView) getView().findViewById(R.id.race_running_base_individual_recall_label);
        resetCourseButton = (Button) getView().findViewById(R.id.race_running_base_reset_course);
        
        if (getRacingProcedure().hasIndividualRecall()) {
            individualRecallButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    RacingProcedure procedure = getRacingProcedure();
                    if (procedure.isIndividualRecallDisplayed()) {
                        procedure.removeIndividualRecall(MillisecondsTimePoint.now());
                    } else {
                        procedure.displayIndividualRecall(MillisecondsTimePoint.now());
                    }
                }
            });
            onIndividualRecallChanged(getRacingProcedure().isIndividualRecallDisplayed());
        } else {
            individualRecallButton.setVisibility(View.GONE);
            individualRecallLabel.setVisibility(View.GONE);
        }
        
        generalRecallButton = (ImageButton) getView().findViewById(R.id.race_running_base_general_recall);
        generalRecallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setTargetFragment(BaseRunningRaceFragment.this, 1);
                confirmDialog.setCallback(new ConfirmRecallListener() {
                    @Override
                    public void returnAddedElementToPicker(boolean recall) {
                        TimePoint now = MillisecondsTimePoint.now();
                        getRaceState().setGeneralRecall(now);
                        // TODO see bug 1649: Explicit passing of pass identifier in RaceState interface
                        getRaceState().setAdvancePass(now);
                    }
                });
                final String tag = "confirm_general_recall_fragment";
                confirmDialog.show(getFragmentManager(), tag);
                
            }
        });
        
        finishingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setFinishingTime();
            }
        });
        
        abortButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RaceDialogFragment fragment = new AbortTypeSelectionDialog();
                fragment.setArguments(getRecentArguments());
                fragment.show(getFragmentManager(), "dialogAPNovemberMode");
            }
        });
        
        resetCourseButton.setOnClickListener(new OnClickListener() {
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
            long millisecondsSinceStart = now.minus(startTime.asMillis()).asMillis();
            
            startCountUpTextView.setText(String.format(
                    getString(R.string.race_running_since_template),
                    getRace().getName(), TimeUtils.formatDurationSince(millisecondsSinceStart)));
            
            if (!updateFlagChangesCountdown(nextCountdownTextView)) {
                nextCountdownTextView.setText("");
            }
        }
        super.notifyTick();
    }

    protected int getActionsLayoutId() {
        return 0;
    }
    
    @Override
    protected void onIndividualRecallChanged(boolean displayed) {
        int textId = displayed ? (R.string.choose_xray_flag_down) : (R.string.choose_xray_flag_up);
        individualRecallLabel.setText(getString(textId));
    }
    
    @Override
    protected void setupUi() {
        ProcedureType procedure = getRacingProcedure();
        TimePoint startTime = getRaceState().getStartTime();
        if (startTime != null) {
            flagRenderer.render(procedure.getActiveFlags(startTime, MillisecondsTimePoint.now()));
        }
    }

    protected void setFinishingTime() {
        RaceDialogFragment fragment = new RaceFinishingTimeDialog();
        fragment.setArguments(getRecentArguments());
        fragment.show(getFragmentManager(), "dialogFinishingTime");
    }
    

}
