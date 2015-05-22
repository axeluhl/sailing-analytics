package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finishing;

import android.os.Bundle;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ess.ESSRacingProcedure;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.PositioningFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ESSFinishingRaceFragment extends BaseFinishingRaceFragment<ESSRacingProcedure> {

    private PositioningFragment positioningFragment;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        positioningFragment = new PositioningFragment();
        positioningFragment.setArguments(PositioningFragment.createArguments(getRace()));
        getFragmentManager().beginTransaction().replace(R.id.race_finishing_inner_container, positioningFragment, null).commit();
    }
    
    @Override
    protected String updateAdditionalInfoText() {
        TimePoint timeLimitAt = getTimeLimit();
        if (timeLimitAt != null) {
            TimePoint finishingStartedAt = getRaceState().getFinishingTime();
            return String.format(getString(R.string.race_first_finisher_and_time_limit), 
                    TimeUtils.formatTime(finishingStartedAt),
                    TimeUtils.formatTime(timeLimitAt));
        }
        return super.updateAdditionalInfoText();
    }

    private TimePoint getTimeLimit() {
        TimePoint startTime = getRaceState().getStartTime();
        if (startTime != null) {
            return getRacingProcedure().getTimeLimit(startTime);
        }
        return null;
    }
    
    @Override
    protected void setFinishedTime() {
        getRaceState().setFinishedTime(MillisecondsTimePoint.now());
    }
}
