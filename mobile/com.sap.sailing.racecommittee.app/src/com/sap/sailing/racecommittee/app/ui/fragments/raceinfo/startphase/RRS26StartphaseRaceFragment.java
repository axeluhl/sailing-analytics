package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;
import android.widget.ImageButton;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26ChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.ReadonlyRRS26RacingProcedure;

public class RRS26StartphaseRaceFragment extends BaseStartphaseRaceFragment<RRS26RacingProcedure> {
    
    private ImageButton startModeButton;
    private final ChangeListener changeListener;
    
    public RRS26StartphaseRaceFragment() {
        this.changeListener = new ChangeListener();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onStart() {
        super.onStart();

//        getRacingProcedure().addChangedListener(changeListener);
    }
    
    @Override
    public void onStop() {
        super.onStop();

//        getRacingProcedure().removeChangedListener(changeListener);
    }

    @Override
    protected void setupUi() {
        // TODO: Maybe check for something like getRacingProcedure().isStartmodeFlagUp()
        super.setupUi();
    }
    
    private class ChangeListener extends BaseRacingProcedureChangedListener implements RRS26ChangedListener {

        @Override
        public void onStartmodeChanged(ReadonlyRRS26RacingProcedure racingProcedure) {
            setupUi();
        }
        
    }

}
