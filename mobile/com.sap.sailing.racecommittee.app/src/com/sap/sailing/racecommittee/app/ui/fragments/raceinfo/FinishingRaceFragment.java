package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.Date;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;

public class FinishingRaceFragment extends RaceFragment {
    
    private TextView countUpTextView;
    protected TextView nextFlagCountdown;
    private ImageButton abortingFlagButton;
    private ImageButton blueFlagButton;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_finishing_view, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        countUpTextView = (TextView) getView().findViewById(R.id.raceCountUp);
        nextFlagCountdown = (TextView) getView().findViewById(R.id.nextFlagCountdown);
        nextFlagCountdown.setText(getTimeLimitText());
        
        blueFlagButton = (ImageButton) getView().findViewById(R.id.blueFlagButton);
        abortingFlagButton = (ImageButton) getView().findViewById(R.id.abortingFlagButton);
        
        PositioningFragment positioningFragment = new PositioningFragment();
        positioningFragment.setArguments(PositioningFragment.createArguments(getRace()));
        getFragmentManager().beginTransaction().add(R.id.innerFragmentHolder, positioningFragment, null).commit();
        
        blueFlagButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showRemoveBlueFlagDialog();
            }
        });

        abortingFlagButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                showNovemberModeDialog();
                ExLog.i(ExLog.FLAG_NOVEMBER, getRace().getId().toString(), getActivity());
            }
        });
        
    }
    
    protected void setCountdownLabels(long millisecondsSinceStart) {
        setStarttimeCountupLabel(millisecondsSinceStart);
    }

    private void setStarttimeCountupLabel(long millisecondsSinceStart) {
        countUpTextView.setText(String.format(getActivity().getResources().getString(R.string.race_running_since_template),
                prettyTimeString(millisecondsSinceStart), getRace().getName()));
    }

    protected CharSequence prettyTimeString(long time) {
        int secondsStart = (int) (time / 1000);
        int hours = secondsStart / 3600;
        int minutes = (secondsStart % 3600) / 60;
        int seconds = (secondsStart % 60);
        String timePattern = "%s:%s:%s";
        String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
        String minutesString = minutes < 10 ? "0" + minutes : "" + minutes;
        String hoursString = hours < 10 ? "0" + hours : "" + hours;
        return String.format(timePattern, hoursString, minutesString,
                secondsString);
    }

    private TimePoint getTimeLimit() {
        TimePoint startTime = getRace().getState().getStartTime();
        TimePoint firstBoatTime = getRace().getState().getFinishingStartTime();
        if (startTime == null || firstBoatTime == null) {
            return null;
        }
        return firstBoatTime.plus((long)((firstBoatTime.asMillis() - startTime.asMillis()) * 0.75));
    }

    private CharSequence getTimeLimitText() {
        TimePoint timeLimit = getTimeLimit();
        if (timeLimit != null) {
            return String.format(getString(R.string.race_first_finisher_and_time_limit),
                    getFormattedTime(getRace().getState().getFinishingStartTime().asDate()), getFormattedTime(timeLimit.asDate()));
        }
        return getString(R.string.empty);
    }

    private String getFormattedTime(Date time) {
        return getFormattedTimePart(time.getHours()) + ":" + getFormattedTimePart(time.getMinutes()) + ":" + getFormattedTimePart(time.getSeconds());
    }

    private String getFormattedTimePart(int timePart) {
        return (timePart < 10) ? "0" + timePart : String.valueOf(timePart);
    }
    
    protected void showNovemberModeDialog() {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new AbortModeSelectionDialog();

        Bundle args = getParameterBundle();
        args.putString(AppConstants.FLAG_KEY, Flags.NOVEMBER.name());
        fragment.setArguments(args);

        fragment.show(fragmentManager, "dialogNovemberMode");
    }

    private void showRemoveBlueFlagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getResources().getString(R.string.confirmation_blue_flag_remove))
        .setCancelable(true)
        .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.FLAG_BLUE_REMOVE, getRace().getId().toString(), getActivity());
                getRace().getState().getStartProcedure().setFinished(MillisecondsTimePoint.now());
                //getRace().getState().setFinishPositioningConfirmed();
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
    
    @Override
    public void onStart() {
        super.onStart();
        ExLog.w(FinishingRaceFragment.class.getName(), String.format("Fragment %s is now shown", FinishingRaceFragment.class.getName()));
    }

    public void notifyTick() {
        if (getRace().getState().getStartTime() == null)
            return;

        long millisSinceStart = System.currentTimeMillis() - getRace().getState().getStartTime().asMillis();
        setCountdownLabels(millisSinceStart);
    }
}
