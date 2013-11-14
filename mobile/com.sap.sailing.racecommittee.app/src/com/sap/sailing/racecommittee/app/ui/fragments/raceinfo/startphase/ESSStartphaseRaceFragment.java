package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.racelog.state.racingprocedure.ESSRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.racecommittee.app.R;

public class ESSStartphaseRaceFragment extends BaseStartphaseRaceFragment<ESSRacingProcedure> {
    
    private ImageView apFlagImageView;
    private ImageView threeFlagImageView;
    private ImageView twoFlagImageView;
    private ImageView oneFlagImageView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        apFlagImageView = createFlagImageView(R.drawable.ap_flag);
        threeFlagImageView = createFlagImageView(R.drawable.three_min_flag);
        twoFlagImageView = createFlagImageView(R.drawable.two_min_flag);
        oneFlagImageView = createFlagImageView(R.drawable.one_min_flag);
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
            break;
        case ESSTWO:
            container.addView(twoFlagImageView);
            break;
        case ESSONE:
            container.addView(oneFlagImageView);
            break;
        default:
            break;
        }
    }

}
