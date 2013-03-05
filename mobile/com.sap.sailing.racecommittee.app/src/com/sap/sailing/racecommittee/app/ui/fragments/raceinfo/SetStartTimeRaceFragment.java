package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

public class SetStartTimeRaceFragment extends RaceFragment implements TickListener {

    public static SetStartTimeRaceFragment create(ManagedRace race) {
        SetStartTimeRaceFragment fragment = new SetStartTimeRaceFragment();
        fragment.setArguments(createArguments(race));
        return fragment;
    }

    protected boolean isReset;

    protected TimePicker startTimePicker;
    protected Button setStartTimeButton;
    protected TextView countdownView;
    protected ImageButton abortRaceButton;

    protected Date scheduledTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_reset_time, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isReset = getArguments().getBoolean(AppConstants.RESET_TIME_FRAGMENT_IS_RESET);

        startTimePicker = (TimePicker) getView().findViewById(R.id.timePicker);
        setStartTimeButton = (Button) getView().findViewById(R.id.btnRescheduleTime);
        countdownView = (TextView) getView().findViewById(R.id.time_below_picker);
        abortRaceButton = (ImageButton) getView().findViewById(R.id.resetTimeAPButton);

        startTimePicker.setIs24HourView(true);
        startTimePicker.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                refreshDifferenceTime();
            }
        });
        setStartTimeButton.setText(isReset ? R.string.reset_time : R.string.set_time);
        setStartTimeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                setStartTime();
            }
        });

        // / TODO: click-listener for abort button

        refreshTimePickerTime();
    }

    @Override
    public void onStart() {
        super.onStart();
        TickSingleton.INSTANCE.registerListener(this);
    }

    @Override
    public void onStop() {
        TickSingleton.INSTANCE.unregisterListener(this);
        super.onStop();
    }

    public void notifyTick() {
        refreshDifferenceTime();
    }

    private void refreshTimePickerTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, AppConstants.DefaultStartTimeMinuteOffset);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        startTimePicker.setCurrentHour(hour);
        startTimePicker.setCurrentMinute(minute);
        refreshDifferenceTime();
    }

    private void refreshDifferenceTime() {
        // This method might be called by the ticker before initialization happens...
        if (startTimePicker == null || countdownView == null) {
            return;
        }

        // / TODO: Why is this method synchronized?
        synchronized (this) {
            int hourOfDay = startTimePicker.getCurrentHour();
            int minute = startTimePicker.getCurrentMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            Date nowDate = calendar.getTime();

            Date pickedDate = (Date) nowDate.clone();
            pickedDate.setHours(hourOfDay);
            pickedDate.setMinutes(minute);
            pickedDate.setSeconds(0);

            long nowTime = nowDate.getTime();
            long pickedTime = pickedDate.getTime();
            long diffTime = pickedTime - nowTime;
            if (diffTime >= 0 && diffTime > 12 * 60 * 60 * 1000) {
                diffTime -= (24 * 60 * 60 * 1000);
            } else if (diffTime < 0 && diffTime < -(12 * 60 * 60 * 1000)) {
                diffTime += (24 * 60 * 60 * 1000);
            }

            long diffHours = diffTime / 1000 / 60 / 60;
            long diffMins = (diffTime / 1000 / 60) % 60;
            long diffSecs = (diffTime / 1000) % 60;

            String minusInd = (diffHours < 0 || diffMins < 0 || diffSecs < 0 ? "-" : "");

            countdownView.setText(getString(R.string.time_until_start) + ": " + minusInd + Math.abs(diffHours) + "h "
                    + Math.abs(diffMins) + "min " + Math.abs(diffSecs) + "sec");
        }
    }

    protected void setStartTime() {
        if (isReset) {
            ExLog.i(ExLog.RACE_RESET_TIME, getRace().getId().toString(), getActivity());
        } else {
            ExLog.i(ExLog.RACE_SET_TIME, getRace().getId().toString(), getActivity());
        }

        Calendar newStartTime = Calendar.getInstance();
        newStartTime.set(Calendar.HOUR_OF_DAY, startTimePicker.getCurrentHour());
        newStartTime.set(Calendar.MINUTE, startTimePicker.getCurrentMinute());
        newStartTime.set(Calendar.SECOND, 0);
        newStartTime.set(Calendar.MILLISECOND, 0);

        setStartTime(newStartTime.getTime());

        // TODO: start course designer activity!
    }

    private void setStartTime(Date newStartTime) {
        getRace().getState().setStartTime(new MillisecondsTimePoint(newStartTime));
    }

}
