package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.Result;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceTimeChangeFragment extends BaseFragment implements View.OnClickListener {

    public final static int START_TIME_MODE = 0;
    public final static int FINISHING_TIME_MODE = 1;
    public final static int FINISHED_TIME_MODE = 2;
    private final static String START_MODE = "startMode";
    private static final int FUTURE_DAYS_DEFAULT = 3;
    private static final int PAST_DAYS_DEFAULT = -3;

    private NumberPicker mDatePicker;
    private TimePicker mTimePicker;
    private NumberPicker mSecondPicker;

    private int mFutureDays = FUTURE_DAYS_DEFAULT;
    private int mPastDays = PAST_DAYS_DEFAULT;

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

        final Calendar calendar = Calendar.getInstance();
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
            Calendar start = (Calendar) calendar.clone();
            Calendar finishing = (Calendar) calendar.clone();
            Calendar finished = (Calendar) calendar.clone();
            switch (getArguments().getInt(START_MODE, 0)) {
                case START_TIME_MODE:
                    finishing.setTime(getRaceState().getFinishingTime().asDate());
                    mFutureDays = TimeUtils.daysBetween(finishing, calendar);
                    break;

                case FINISHING_TIME_MODE:
                    start.setTime(getRaceState().getStartTime().asDate());
                    mPastDays = TimeUtils.daysBetween(start, calendar);
                    finished.setTime(getRaceState().getFinishedTime().asDate());
                    mFutureDays = TimeUtils.daysBetween(finished, calendar);
                    break;

                case FINISHED_TIME_MODE:
                    finishing.setTime(getRaceState().getFinishingTime().asDate());
                    mPastDays = TimeUtils.daysBetween(finishing, calendar);
                    break;

                default:
                    // use default values
            }
            ViewHelper.disableSave(mDatePicker);
            ThemeHelper.setPickerColor(getActivity(), mDatePicker, ThemeHelper.getColor(getActivity(), R.attr.white), ThemeHelper
                .getColor(getActivity(), R.attr.sap_yellow_1));
            TimeUtils.initDatePicker(getActivity(), mDatePicker, calendar, mPastDays, mFutureDays, false);
            mDatePicker.setValue(Math.abs(mPastDays));
            mDatePicker.setTag(calendar);
            if (mDatePicker.getMinValue() == mDatePicker.getMaxValue()) {
                mDatePicker.setVisibility(View.GONE);
            }
        }

        mTimePicker = (TimePicker) layout.findViewById(R.id.time_picker);
        if (mTimePicker != null) {
            ViewHelper.disableSave(mTimePicker);
            ThemeHelper.setPickerColor(getActivity(), mTimePicker, ThemeHelper.getColor(getActivity(), R.attr.white), ThemeHelper
                .getColor(getActivity(), R.attr.sap_yellow_1));
            mTimePicker.setIs24HourView(true);
            mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }

        mSecondPicker = (NumberPicker) layout.findViewById(R.id.second_picker);
        if (mSecondPicker != null) {
            ViewHelper.disableSave(mSecondPicker);
            ThemeHelper.setPickerColor(getActivity(), mSecondPicker, ThemeHelper.getColor(getActivity(), R.attr.white), ThemeHelper
                .getColor(getActivity(), R.attr.sap_yellow_1));
            mSecondPicker.setVisibility(View.VISIBLE);
            mSecondPicker.setMinValue(0);
            mSecondPicker.setMaxValue(59);
            mSecondPicker.setValue(calendar.get(Calendar.SECOND));
            mSecondPicker.setFormatter(new NumberPicker.Formatter() {
                @Override
                public String format(int i) {
                    return String.format("%02d", i);
                }
            });
            mSecondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    if (mTimePicker != null) {
                        Calendar pickerTime = (Calendar) calendar.clone();
                        pickerTime.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
                        pickerTime.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
                        if (oldVal == 0 && newVal == 59) {
                            pickerTime.add(Calendar.MINUTE, -1);
                        }
                        if (oldVal == 59 && newVal == 0) {
                            pickerTime.add(Calendar.MINUTE, 1);
                        }
                        mTimePicker.setCurrentHour(pickerTime.get(Calendar.HOUR_OF_DAY));
                        mTimePicker.setCurrentMinute(pickerTime.get(Calendar.MINUTE));
                    }
                }
            });
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
        Calendar calendar = (Calendar) mDatePicker.getTag();
        calendar.add(Calendar.DAY_OF_MONTH, mDatePicker.getValue() + mPastDays);
        calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
        calendar.set(Calendar.SECOND, mSecondPicker.getValue());
        calendar.set(Calendar.MILLISECOND, 0);
        return new MillisecondsTimePoint(calendar.getTime());
    }

    private void closeFragment() {
        Intent intent = new Intent(AppConstants.INTENT_ACTION_SHOW_SUMMARY_CONTENT);
        BroadcastManager.getInstance(getActivity()).addIntent(intent);
    }
}
