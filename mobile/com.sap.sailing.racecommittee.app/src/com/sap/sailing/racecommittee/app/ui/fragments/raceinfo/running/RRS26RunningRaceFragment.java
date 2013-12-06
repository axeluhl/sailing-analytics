package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import android.view.View;
import android.widget.Toast;

import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.ReadonlyRRS26RacingProcedure;

public class RRS26RunningRaceFragment extends BaseRunningRaceFragment<RRS26RacingProcedure> {

    private RRS26ChangedListener changeListener;

    public RRS26RunningRaceFragment() {
        this.changeListener = new ChangeListener();
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

    private class ChangeListener extends BaseRacingProcedureChangedListener implements RRS26ChangedListener {

        @Override
        public void onStartmodeChanged(ReadonlyRRS26RacingProcedure racingProcedure) {
            // that's a little bit late, isn't it?
            Toast.makeText(getActivity(),
                    String.format("Start mode flag changed to %s", racingProcedure.getStartModeFlag()),
                    Toast.LENGTH_SHORT).show();
            
            int viewMode = racingProcedure.hasIndividualRecall() ? View.VISIBLE : View.GONE;
            if (racingProcedure.hasIndividualRecall()) {
                individualRecallButton.setVisibility(viewMode);
                individualRecallLabel.setVisibility(viewMode);
            }
        }

    }

}
