package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.PositioningFragment;

public class EssFinishedRaceFragment extends FinishedRaceFragment {
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
        TimePoint startTime = getRace().getState().getStartTime();
        TimePoint firstBoatTime = getRace().getState().getFinishingStartTime();
        if (startTime == null || firstBoatTime == null) {
            return null;
        }
        return firstBoatTime.plus((long) ((firstBoatTime.asMillis() - startTime.asMillis()) * 0.75));
    }

    protected CharSequence getTimeLimitText() {
        TimePoint timeLimit = getTimeLimit();
        if (timeLimit != null) {
            return String.format(getString(R.string.race_time_limit), getFormattedTime(timeLimit.asDate()));
        }
        return getString(R.string.empty);
    }
}
