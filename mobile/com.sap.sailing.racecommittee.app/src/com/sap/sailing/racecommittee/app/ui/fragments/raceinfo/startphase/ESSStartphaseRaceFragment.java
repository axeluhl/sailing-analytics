package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.state.racingprocedure.ess.ESSRacingProcedure;

public class ESSStartphaseRaceFragment extends BaseStartphaseRaceFragment<ESSRacingProcedure> {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        raceStartIn4Minutes.setVisibility(View.VISIBLE);
        raceStartIn4Minutes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final TimePoint now = MillisecondsTimePoint.now();

                getRaceState().setAdvancePass(now);
                getRaceState().setRacingProcedure(now, RacingProcedureType.ESS);
                getRaceState().forceNewStartTime(now, now.plus(4*60*1000));
            }
        });
    }
        
}
