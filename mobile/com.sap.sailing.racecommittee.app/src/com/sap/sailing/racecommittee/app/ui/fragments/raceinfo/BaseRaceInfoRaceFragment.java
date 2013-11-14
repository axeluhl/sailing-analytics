package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.Activity;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public abstract class BaseRaceInfoRaceFragment<ProcedureType extends RacingProcedure> extends RaceFragment {

    private final ChangeListener changeListener;
    protected RaceInfoListener infoListener;
    
    public BaseRaceInfoRaceFragment() {
        this.changeListener = new ChangeListener();
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
        getRacingProcedure().addChangedListener(changeListener);
    }
    
    @Override
    public void onStop() {
        getRacingProcedure().removeChangedListener(changeListener);
        super.onStop();
    }
    
    protected ProcedureType getRacingProcedure() {
        return getRaceState().getTypedRacingProcedure();
    }
    
    protected abstract void setupUi();
    
    private class ChangeListener extends BaseRacingProcedureChangedListener {
        @Override
        public void onActiveFlagsChanged(RacingProcedure racingProcedure) {
            setupUi();
        }
    }
}
