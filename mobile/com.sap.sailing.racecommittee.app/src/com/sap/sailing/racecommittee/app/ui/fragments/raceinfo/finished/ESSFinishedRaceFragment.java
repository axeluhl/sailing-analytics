package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ess.ESSRacingProcedure;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.PositioningFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public class ESSFinishedRaceFragment extends BaseFinishedRaceFragment<ESSRacingProcedure> {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button positioningButton = (Button) getView().findViewById(R.id.buttonPositioning);
        positioningButton.setVisibility(Button.VISIBLE);
        positioningButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PositioningFragment fragment = new PositioningFragment();
                fragment.setArguments(PositioningFragment.createArguments(getRace()));
                fragment.show(getFragmentManager(), null);
            }
        });
    }
    
    private TimePoint getTimeLimit() {
        TimePoint startTime = getRaceState().getStartTime();
        TimePoint firstBoatTime = getRaceState().getFinishingTime();
        if (startTime == null || firstBoatTime == null) {
            return null;
        }
        return firstBoatTime.plus((long) ((firstBoatTime.asMillis() - startTime.asMillis()) * 0.75));
    }

    protected CharSequence getTimeLimitText() {
        TimePoint timeLimit = getTimeLimit();
        if (timeLimit != null) {
            return String.format(getString(R.string.race_time_limit), TimeUtils.formatTime(timeLimit));
        }
        return getString(R.string.empty);
    }
}
