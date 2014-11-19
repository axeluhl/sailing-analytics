package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.racelog.state.racingprocedure.ess.ESSRacingProcedure;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ESSStartphaseRaceFragment extends BaseStartphaseRaceFragment<ESSRacingProcedure> {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        raceStartIn4Minutes = (Button) getView().findViewById(R.id.raceStartIn4Minutes);
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
        raceStartIn2Minutes = (Button) getView().findViewById(R.id.raceStartIn1Minute);
        raceStartIn2Minutes.setVisibility(View.VISIBLE);
        raceStartIn2Minutes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final TimePoint now = MillisecondsTimePoint.now();
                getRaceState().setAdvancePass(now);
                getRaceState().setRacingProcedure(now, RacingProcedureType.ESS);
                getRaceState().forceNewStartTime(now, now.plus(1*60*1000));
            }
        });
        resetTimeButton.setVisibility(View.INVISIBLE);
        toggleGroupRacing = (ToggleButton) getView().findViewById(R.id.toggle_group_race_mode);
        toggleGroupRacing.setVisibility(View.VISIBLE);
        toggleGroupRacing.setChecked(getRaceState().isAdditionalScoringInformationEnabled(AdditionalScoringInformationType.MAX_POINTS_DECREASE_MAX_SCORE));
        toggleGroupRacing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getRaceState().setAdditionalScoringInformationEnabled(MillisecondsTimePoint.now(), /*enable*/isChecked, AdditionalScoringInformationType.MAX_POINTS_DECREASE_MAX_SCORE);
            }
        });
    }
        
}
