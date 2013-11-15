package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.List;

import android.app.Activity;
import android.widget.TextView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public abstract class BaseRaceInfoRaceFragment<ProcedureType extends RacingProcedure> extends RaceFragment {

    private final ProcedureChangedListener procedureListener;
    protected RaceInfoListener infoListener;
    
    public BaseRaceInfoRaceFragment() {
        this.procedureListener = new ProcedureChangedListener();
    }
    
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
    public void onStart() {
        super.onStart();
        setupUi();
        getRacingProcedure().addChangedListener(procedureListener);
    }
    
    @Override
    public void onStop() {
        getRacingProcedure().removeChangedListener(procedureListener);
        super.onStop();
    }
    
    protected ProcedureType getRacingProcedure() {
        return getRaceState().getTypedRacingProcedure();
    }
    
    protected abstract void setupUi();

    
    protected boolean setFlagChangesCountdown(TextView targetView) {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint startTime = getRaceState().getStartTime();
        FlagPoleState flagState = getRaceState().getRacingProcedure().getActiveFlags(startTime, now);
        List<FlagPole> flagChanges = flagState.computeUpcomingChanges();
        if (!flagChanges.isEmpty()) {
            FlagPole changedFlag = flagChanges.get(0);
            long millisecondsTillChange = TimeUtils.timeUntil(flagState.getNextStateValidFrom());
            
            int formatTextResourceId = changedFlag.isDisplayed() ? R.string.race_startphase_countdown_mode_display : 
                R.string.race_startphase_countdown_mode_remove;
            targetView.setText(getString(formatTextResourceId, TimeUtils.formatDuration(millisecondsTillChange), 
                    changedFlag.getUpperFlag().toString()));
            return true;
        }
        return false;
    }
    
    private class ProcedureChangedListener extends BaseRacingProcedureChangedListener {
        @Override
        public void onActiveFlagsChanged(RacingProcedure racingProcedure) {
            setupUi();
        }
    }
}
