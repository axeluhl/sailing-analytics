package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure2;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceChooseStartModeDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;

public class RRS26StartphaseRaceFragment2 extends BaseStartphaseRaceFragment {
    
    private ImageButton startModeButton;
    private final ChangeListener changeListener;
    
    private ImageView classFlagImageView;
    private ImageView startModeFlagImageView;
    
    public RRS26StartphaseRaceFragment2() {
        this.changeListener = new ChangeListener();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startModeButton = (ImageButton) getView().findViewById(R.id.race_startphase_rrs26_actions_startmode);
        startModeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RaceDialogFragment fragment = new RaceChooseStartModeDialog();
                fragment.setArguments(getRecentArguments());
                fragment.show(getFragmentManager(), "dialogStartMode");
            }
        });
        
        classFlagImageView = new ImageView(getActivity());
        classFlagImageView.setLayoutParams(new LinearLayout.LayoutParams(200, 130));
        classFlagImageView.setImageResource(getClassmageResourceId());
        classFlagImageView.setPadding(6, 6, 6, 6);
        classFlagImageView.setBackgroundColor(getFleetColorId());
        
        startModeFlagImageView = createFlagImageView(getStartModeImageResourceId());
    }
    
    @Override
    public void onStart() {
        super.onStart();
        getRacingProcedure().addChangedListener(changeListener);
    }
    
    @Override
    public void onStop() {
        getRacingProcedure().removeChangedListener(changeListener);
        super.onStop();
    }
    
    
    @Override
    protected int getActionsLayoutId() {
        return R.layout.race_startphase_rrs26_actions;
    }

    @Override
    protected void setupUi() {
        lowerFlagsViewGroup.removeAllViews();
        upperFlagsViewGroup.removeAllViews();
        
        TimePoint startTime = getRaceState().getStartTime();
        if (startTime == null) {
            return;
        }
        
        startModeFlagImageView.setImageResource(getStartModeImageResourceId());
        FlagPoleState activeFlags = getRacingProcedure().getActiveFlags(startTime, MillisecondsTimePoint.now());
        Flags startModeFlag = getRacingProcedure().getStartModeFlag();
        
        boolean isClassFlagUp = false;
        boolean isStartModeFlagUp = false;
        
        for (FlagPole pole : activeFlags.getCurrentState()) {
            if (pole.isDisplayed()) {
                if (pole.getUpperFlag().equals(Flags.CLASS)) {
                    isClassFlagUp = true;
                } else if (pole.getUpperFlag().equals(startModeFlag)) {
                    isStartModeFlagUp = true;
                }
            }
        }
        
        if (isClassFlagUp) {
            upperFlagsViewGroup.addView(classFlagImageView);
        } else {
            lowerFlagsViewGroup.addView(classFlagImageView);
        }
        if (isStartModeFlagUp) {
            
            upperFlagsViewGroup.addView(startModeFlagImageView);
        } else {
            lowerFlagsViewGroup.addView(startModeFlagImageView);
        }
        startModeButton.setEnabled(!isStartModeFlagUp);
    }

    private int getStartModeImageResourceId() {
        switch (getRacingProcedure().getStartModeFlag()) {
        case PAPA:
            return R.drawable.papa_flag;
        case INDIA:
            return R.drawable.india_flag;
        case BLACK:
            return R.drawable.black_flag;
        case ZULU:
            return R.drawable.zulu_flag;
        default:
            return R.drawable.papa_flag;
        }
    }
    
    private int getClassmageResourceId() {
        // TODO: check boat class for specific image
        return R.drawable.generic_class;
    }

    private int getFleetColorId() {
        Triple<Integer, Integer, Integer> rgb = getRace().getFleet().getColor().getAsRGB();
        int color = Color.rgb(rgb.getA(), rgb.getB(), rgb.getC());
        return color;
    }
    
    private RRS26RacingProcedure getRacingProcedure() {
        return getRaceState().getTypedRacingProcedure();
    }
    
    private class ChangeListener extends BaseRacingProcedureChangedListener implements RRS26ChangedListener {

        @Override
        public void onStartmodeChanged(RRS26RacingProcedure racingProcedure) {
            setupUi();
        }
        
        @Override
        public void onActiveFlagsChanged(RacingProcedure2 racingProcedure) {
            setupUi();
        }
        
    }

}
