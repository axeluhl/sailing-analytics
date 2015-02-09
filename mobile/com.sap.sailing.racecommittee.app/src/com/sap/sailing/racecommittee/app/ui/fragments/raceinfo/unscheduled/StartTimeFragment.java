package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StartTimeFragment extends RaceFragment implements View.OnClickListener {

    private static final String TAG = StartTimeFragment.class.getName();
    private static final String SHOWBACK = "showBack";

    private DatePicker mDatePicker;
    private TimePicker mTimePicker;

    public static StartTimeFragment newInstance(boolean showBack) {
        StartTimeFragment fragment = new StartTimeFragment();
        Bundle args = new Bundle();
        args.putBoolean(SHOWBACK, showBack);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_start_time, container, false);

        if (getArguments() != null) {
            if(getArguments().getBoolean(SHOWBACK, false)) {
                View header = view.findViewById(R.id.header_text);
                if (header != null) {
                    header.setOnClickListener(this);
                }

                View back = view.findViewById(R.id.header_back);
                if (back != null) {
                    back.setVisibility(View.VISIBLE);
                }
            }
        }

        Button min5 = (Button) view.findViewById(R.id.start_min_five);
        if (min5 != null) {
            min5.setOnClickListener(this);
        }

        Button min1 = (Button) view.findViewById(R.id.start_min_one);
        if (min1 != null) {
            min1.setOnClickListener(this);
        }

        Button setStart = (Button) view.findViewById(R.id.set_start_time);
        if (setStart != null) {
            setStart.setOnClickListener(this);
        }

        mDatePicker = (DatePicker) view.findViewById(R.id.start_date_picker);
        if (mDatePicker != null) {
            mDatePicker.setCalendarViewShown(false);
            mDatePicker.setMinDate(System.currentTimeMillis() - 1000);
        }

        mTimePicker = (TimePicker) view.findViewById(R.id.start_time_picker);
        if (mTimePicker != null) {
            mTimePicker.setIs24HourView(true);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TimePoint protestTime = null;
        if (getRace() != null) {
            if (getRace().getState() != null) {
                protestTime = getRace().getState().getProtestTime();
            }
        }

        if (protestTime != null) {
            if (mDatePicker != null) {
                mDatePicker.getCalendarView().setDate(protestTime.asMillis());
            }

            if (mTimePicker != null) {
                mTimePicker.setCurrentHour(protestTime.asDate().getHours());
                mTimePicker.setCurrentMinute(protestTime.asDate().getMinutes());
            }
        }
    }

    @Override
    public void onClick(View view) {
        TimePoint now = new MillisecondsTimePoint(new Date());
        Calendar calendar = Calendar.getInstance();
        switch (view.getId()) {
            case R.id.start_min_five:
            case R.id.start_min_one:
                Integer minutes;
                try {
                    minutes = Integer.valueOf("" + view.getTag());
                } catch (Exception ex) {
                    minutes = 0;
                }
                now = now.plus(TimeUnit.MINUTES.toMillis(minutes));

                if (mDatePicker != null) {
                    mDatePicker.getCalendarView().setDate(now.asMillis());
                }

                if (mTimePicker != null) {
                    mTimePicker.setCurrentHour(now.asDate().getHours());
                    mTimePicker.setCurrentMinute(now.asDate().getMinutes());
                }
                break;

            case R.id.set_start_time:
                calendar.set(Calendar.DAY_OF_MONTH, mDatePicker.getDayOfMonth());
                calendar.set(Calendar.MONTH, mDatePicker.getMonth());
                calendar.set(Calendar.YEAR, mDatePicker.getYear());
                calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
                calendar.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());

                getRace().getState().setProtestTime(now, new MillisecondsTimePoint(calendar.getTimeInMillis()));
                openMainFragment();
                break;

            case R.id.header_text:
                openMainFragment();
                break;

            default:
                break;
        }
    }

    private void openMainFragment() {
        RaceFragment fragment = MainScheduleFragment.newInstance();
        fragment.setArguments(RaceFragment.createArguments(getRace()));
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.racing_view_container, fragment)
                .commit();
    }
}
