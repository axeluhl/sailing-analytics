package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.Activity;

import com.sap.sailing.domain.racelog.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;

public abstract class BaseRaceInfoRaceFragment<ProcedureType extends RacingProcedure> extends RaceFragment {

    private final ProcedureChangedListener procedureListener;
    private final StateChangedListener stateListener;
    protected RaceInfoListener infoListener;
    
    public BaseRaceInfoRaceFragment() {
        this.procedureListener = new ProcedureChangedListener();
        this.stateListener = new StateChangedListener();
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
        getRaceState().addChangedListener(stateListener);
    }
    
    @Override
    public void onStop() {
        getRacingProcedure().removeChangedListener(procedureListener);
        getRaceState().removeChangedListener(stateListener);
        super.onStop();
    }
    
    protected ProcedureType getRacingProcedure() {
        return getRaceState().getTypedRacingProcedure();
    }
    
    protected abstract void setupUi();
    
    private class ProcedureChangedListener extends BaseRacingProcedureChangedListener {
        @Override
        public void onActiveFlagsChanged(RacingProcedure racingProcedure) {
            setupUi();
        }
    }
    
    private class StateChangedListener extends BaseRaceStateChangedListener {
        
    }
}
