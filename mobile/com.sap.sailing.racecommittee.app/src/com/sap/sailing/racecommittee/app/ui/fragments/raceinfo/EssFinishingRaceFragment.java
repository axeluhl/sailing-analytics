package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.PositioningFragment;

public class EssFinishingRaceFragment extends FinishingRaceFragment {

    private PositioningFragment positioningFragment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        positioningFragment = new PositioningFragment();
        positioningFragment.setArguments(PositioningFragment.createArguments(getRace()));
        getFragmentManager().beginTransaction().replace(R.id.innerFragmentHolder, positioningFragment, null).commit();
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
    
    protected void showRemoveBlueFlagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getResources().getString(R.string.confirmation_blue_flag_remove))
        .setCancelable(true)
        .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.FLAG_BLUE_REMOVE, getRace().getId().toString(), getActivity());
                getRace().getState().getStartProcedure().setFinished(MillisecondsTimePoint.now());
            }
        })
        .setNegativeButton(getActivity().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.FLAG_BLUE_REMOVE_NO, getRace().getId().toString(), getActivity());
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
