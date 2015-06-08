package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.Result;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.Calendar;

public class RaceTimeChangeFragment extends BaseFragment implements View.OnClickListener {

    private final static String START_MODE = "startMode";

    public final static int START_TIME_MODE = 0;
    public final static int FINISHING_TIME_MODE = 1;
    public final static int FINISHED_TIME_MODE = 2;

    private static final int FUTURE_DAYS = 25;
    private static final int PAST_DAYS = -3;

    private NumberPicker mDatePicker;
    private TimePicker mTimePicker;

    public static RaceTimeChangeFragment newInstance(int mode) {
        RaceTimeChangeFragment fragment = new RaceTimeChangeFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_time_change, container, false);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View layout = getView();

        if (layout == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        TextView headerText = (TextView) layout.findViewById(R.id.header_headline);
        if (headerText != null) {
            switch (getArguments().getInt(START_MODE, 0)) {
            case FINISHING_TIME_MODE:
                calendar.setTime(getRaceState().getFinishingTime().asDate());
                headerText.setText(getString(R.string.race_summary_finish_begin));
                break;

            case FINISHED_TIME_MODE:
                calendar.setTime(getRaceState().getFinishedTime().asDate());
                headerText.setText(getString(R.string.race_summary_finish_end));
                break;

            default: // START_TIME_MODE
                calendar.setTime(getRaceState().getStartTime().asDate());
                headerText.setText(getString(R.string.race_summary_start));
                break;
            }
        }

        mDatePicker = (NumberPicker) layout.findViewById(R.id.date_picker);
        if (mDatePicker != null) {
            ThemeHelper.setPickerTextColor(getActivity(), mDatePicker, ThemeHelper.getColor(getActivity(), R.attr.white));
            TimeUtils.initDatePicker(getActivity(), mDatePicker, calendar, PAST_DAYS, FUTURE_DAYS);
            int days = TimeUtils.daysBetween(Calendar.getInstance(), calendar) + Math.abs(PAST_DAYS);
            mDatePicker.setValue(days);
        }

        mTimePicker = (TimePicker) layout.findViewById(R.id.time_picker);
        if (mTimePicker != null) {
            ThemeHelper.setPickerTextColor(getActivity(), mTimePicker, ThemeHelper.getColor(getActivity(), R.attr.white));
            mTimePicker.setIs24HourView(true);
            mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }

        View setTime = layout.findViewById(R.id.set_date_time);
        if (setTime != null) {
            setTime.setOnClickListener(this);
        }

        View header = layout.findViewById(R.id.header);
        if (header != null) {
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeFragment();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        TimePoint time = getPickerTime();

        Result result;
        switch (getArguments().getInt(START_MODE)) {
        case FINISHING_TIME_MODE:
            result = getRace().setFinishingTime(time);
            if (result.hasError()) {
                Toast.makeText(getActivity(), result.getMessage(getActivity()), Toast.LENGTH_LONG).show();
            }
            break;

        case FINISHED_TIME_MODE:
            result = getRace().setFinishedTime(time);
            if (result.hasError()) {
                Toast.makeText(getActivity(), result.getMessage(getActivity()), Toast.LENGTH_LONG).show();
            }
            break;

        default: // START_TIME_MODE
            getRaceState().forceNewStartTime(MillisecondsTimePoint.now(), time);
            break;
        }

        closeFragment();
    }

    private TimePoint getPickerTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, mDatePicker.getValue() + PAST_DAYS);
        calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new MillisecondsTimePoint(calendar.getTime());
    }

    private void closeFragment() {
        Intent intent = new Intent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
