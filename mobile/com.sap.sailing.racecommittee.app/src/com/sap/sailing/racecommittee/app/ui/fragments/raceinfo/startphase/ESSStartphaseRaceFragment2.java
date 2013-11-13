package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.racelog.state.racingprocedure.ESSChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.ESSRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure2;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.R;

public class ESSStartphaseRaceFragment2 extends BaseStartphaseRaceFragment {
    
    private final ChangeListener changeListener;
    
    private ImageView apFlagImageView;
    private ImageView threeFlagImageView;
    private ImageView twoFlagImageView;
    private ImageView oneFlagImageView;
    
    public ESSStartphaseRaceFragment2() {
        this.changeListener = new ChangeListener();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        apFlagImageView = createFlagImageView(R.drawable.ap_flag);
        threeFlagImageView = createFlagImageView(R.drawable.three_min_flag);
        twoFlagImageView = createFlagImageView(R.drawable.two_min_flag);
        oneFlagImageView = createFlagImageView(R.drawable.one_min_flag);
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
    protected void setupUi() {
        lowerFlagsViewGroup.removeAllViews();
        upperFlagsViewGroup.removeAllViews();
        
        TimePoint startTime = getRaceState().getStartTime();
        if (startTime == null) {
            return;
        }
        
        FlagPoleState activeFlags = getRacingProcedure().getActiveFlags(startTime, MillisecondsTimePoint.now());
        for (FlagPole pole : activeFlags.getCurrentState()) {
            addFlagViews(pole, pole.isDisplayed() ? upperFlagsViewGroup : lowerFlagsViewGroup);
        }
    }

    private void addFlagViews(FlagPole pole, ViewGroup container) {
        switch (pole.getUpperFlag()) {
        case AP:
            container.addView(apFlagImageView);;
            break;
        case ESSTHREE:
            container.addView(threeFlagImageView);
        case ESSTWO:
            container.addView(twoFlagImageView);
        case ESSONE:
            container.addView(oneFlagImageView);
        default:
            break;
        }
    }
    
    private ESSRacingProcedure getRacingProcedure() {
        return getRaceState().getTypedRacingProcedure();
    }
    
    private class ChangeListener extends BaseRacingProcedureChangedListener implements ESSChangedListener {

        @Override
        public void onActiveFlagsChanged(RacingProcedure2 racingProcedure) {
            setupUi();
        }
        
    }

}
