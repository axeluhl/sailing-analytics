package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.List;

import android.app.Activity;
import android.util.Pair;
import android.widget.TextView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public abstract class BaseRaceInfoRaceFragment<ProcedureType extends RacingProcedure> extends RaceFragment {

    private final ProcedureChangedListener procedureListener;

    private Pair<FlagPole, TimePoint> cachedNextFlag;
    
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

    
    protected boolean updateFlagChangesCountdown(TextView targetView) {
        if (cachedNextFlag == null) {
            ExLog.i(BaseRaceInfoRaceFragment.class.getName(), "Refilling next-flag cache.");
            TimePoint now = MillisecondsTimePoint.now();
            TimePoint startTime = getRaceState().getStartTime();
            FlagPoleState flagState = getRaceState().getRacingProcedure().getActiveFlags(startTime, now);
            List<FlagPole> flagChanges = flagState.computeUpcomingChanges();
            if (!flagChanges.isEmpty()) {
                TimePoint changeAt = flagState.getNextStateValidFrom();
                FlagPole changePole = getMostInterestingFlagPole(flagChanges);
                
                renderFlagChangesCountdown(targetView, changeAt, changePole);
                cachedNextFlag = new Pair<FlagPole, TimePoint>(changePole, changeAt);
                return true;
            }
            return false;
        } else {
            TimePoint changeAt = cachedNextFlag.second;
            FlagPole changePole = cachedNextFlag.first;
            
            renderFlagChangesCountdown(targetView, changeAt, changePole);
            return true;
        }
    }

    private FlagPole getMostInterestingFlagPole(List<FlagPole> poles) {
        for (FlagPole pole : poles) {
            if (pole.isDisplayed()) {
                return pole;
            }
        }
        return poles.get(0);
    }

    private void renderFlagChangesCountdown(TextView targetView, TimePoint changeAt, FlagPole changePole) {
        long millisecondsTillChange = TimeUtils.timeUntil(changeAt);
        int formatTextResourceId = changePole.isDisplayed() ? R.string.race_startphase_countdown_mode_display : 
            R.string.race_startphase_countdown_mode_remove;
        StringBuilder flagName = new StringBuilder();
        flagName.append(changePole.getUpperFlag().toString());
        if (changePole.getLowerFlag() != Flags.NONE) {
            flagName.append("|");
            flagName.append(changePole.getLowerFlag().toString());
        }
        targetView.setText(getString(formatTextResourceId, TimeUtils.formatDuration(millisecondsTillChange), flagName));
    }
    
    private class ProcedureChangedListener extends BaseRacingProcedureChangedListener {
        @Override
        public void onActiveFlagsChanged(RacingProcedure racingProcedure) {
            setupUi();
            cachedNextFlag = null;
        }
    }
}
