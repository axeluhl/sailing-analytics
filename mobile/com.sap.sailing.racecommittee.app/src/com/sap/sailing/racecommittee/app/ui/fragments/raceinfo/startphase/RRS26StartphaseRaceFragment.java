package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.ReadonlyRRS26RacingProcedure;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.RaceChooseStartModeDialog;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RRS26StartphaseRaceFragment extends BaseStartphaseRaceFragment<RRS26RacingProcedure> {
    
    private ImageButton startModeButton;
    private final ChangeListener changeListener;
    
    public RRS26StartphaseRaceFragment() {
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

        raceStartIn5Minutes = (Button) getView().findViewById(R.id.raceStartIn5Minutes);
        raceStartIn5Minutes.setVisibility(View.VISIBLE);
        raceStartIn5Minutes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final TimePoint now = MillisecondsTimePoint.now();
                getRaceState().setAdvancePass(now);
                getRaceState().setRacingProcedure(now, RacingProcedureType.RRS26);
                getRaceState().forceNewStartTime(now, now.plus(5*60*1000));
            }
        });
        raceStartIn2Minutes = (Button) getView().findViewById(R.id.raceStartIn1Minute);
        raceStartIn2Minutes.setVisibility(View.VISIBLE);
        raceStartIn2Minutes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final TimePoint now = MillisecondsTimePoint.now();
                getRaceState().setAdvancePass(now);
                getRaceState().setRacingProcedure(now, RacingProcedureType.RRS26);
                getRaceState().forceNewStartTime(now, now.plus(1*60*1000));
            }
        });
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
