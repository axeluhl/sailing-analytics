package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.PositioningFragment;

public class EssFinishingRaceFragment extends FinishingRaceFragment {

    private PositioningFragment positioningFragment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        positioningFragment = new PositioningFragment();
        positioningFragment.setArguments(PositioningFragment.createArguments(getRace()));
        getFragmentManager().beginTransaction().add(R.id.innerFragmentHolder, positioningFragment, null).commit();
    }

    private TimePoint getTimeLimit() {
        TimePoint startTime = getRace().getState().getStartTime();
        TimePoint firstBoatTime = getRace().getState().getFinishingStartTime();
        if (startTime == null || firstBoatTime == null) {
            return null;
        }
        return firstBoatTime.plus((long) ((firstBoatTime.asMillis() - startTime.asMillis()) * 0.75));
    }

    protected CharSequence getNextFlagCountDownText() {
        TimePoint timeLimit = getTimeLimit();
        if (timeLimit != null) {
            return String.format(getString(R.string.race_first_finisher_and_time_limit), getFormattedTime(getRace()
                    .getState().getFinishingStartTime().asDate()), getFormattedTime(timeLimit.asDate()));
        }
        return getString(R.string.empty);
    }
}
