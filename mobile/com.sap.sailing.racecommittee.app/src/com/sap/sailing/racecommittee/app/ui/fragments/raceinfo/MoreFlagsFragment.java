package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.MoreFlagsAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.MoreFlagsAdapter.MoreFlag;
import com.sap.sailing.racecommittee.app.ui.adapters.MoreFlagsAdapter.MoreFlagItemClick;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.Calendar;

public class MoreFlagsFragment extends ScheduleFragment implements MoreFlagItemClick {

    private MoreFlagsAdapter mAdapter;

    public MoreFlagsFragment() {

    }

    public static MoreFlagsFragment newInstance() {
        MoreFlagsFragment fragment = new MoreFlagsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.flag_list, container, false);

        ListView listView = (ListView) layout.findViewById(R.id.listView);
        if (listView != null) {
            mAdapter = new MoreFlagsAdapter(getActivity(), this);
            listView.setAdapter(mAdapter);
        }

        return layout;
    }

    @Override
    public void onClick(MoreFlag flag) {
        mAdapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), flag.file_name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMore(MoreFlag flag) {
        switch (flag.flag) {
            case BLUE:
                replaceFragment(FinishingTimeFragment.newInstance(), R.id.race_frame);
                break;

            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

    @Override
    public void onPause() {
        super.onPause();

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    public static class FinishingTimeFragment extends ScheduleFragment implements View.OnClickListener {

        private TimePicker mTimePicker;

        public FinishingTimeFragment() {

        }

        public static FinishingTimeFragment newInstance() {
            FinishingTimeFragment fragment = new FinishingTimeFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.race_finish_config, container, false);

            View header = ViewHolder.get(layout, R.id.header_text);
            if (header != null) {
                header.setOnClickListener(this);
            }

            mTimePicker = (TimePicker) layout.findViewById(R.id.time_picker);
            if (mTimePicker != null) {
                mTimePicker.setIs24HourView(true);
            }

            View currentTime = layout.findViewById(R.id.finish_current);
            if (currentTime != null) {
                currentTime.setOnClickListener(this);
            }

            View customTime = layout.findViewById(R.id.finish_custom);
            if (customTime != null) {
                customTime.setOnClickListener(this);
            }

            return layout;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.finish_current:
                    setFinishTime(true);
                    break;

                case R.id.finish_custom:
                    setFinishTime(false);
                    break;

                default:
                    replaceFragment(MoreFlagsFragment.newInstance(), R.id.race_frame);
                    break;
            }
        }

        private void setFinishTime(boolean current) {
            TimePoint finishingTime;
            if (current) {
                finishingTime = MillisecondsTimePoint.now();
            } else {
                finishingTime = getFinishingTime();
            }
            StartTimeFinder stf = new StartTimeFinder(getRace().getRaceLog());
            if (stf.analyze() != null && getRace().getStatus().equals(RaceLogRaceStatus.RUNNING)) {
                if (stf.analyze().before(finishingTime)) {
                    getRace().getState().setFinishingTime(finishingTime);
                } else {
                    Toast.makeText(getActivity(), "The selected time is before the race start.", Toast.LENGTH_LONG).show();
                }
            }
        }

        private TimePoint getFinishingTime() {
            Calendar time = Calendar.getInstance();
            time.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
            time.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
            time.set(Calendar.SECOND, 0);
            time.set(Calendar.MILLISECOND, 0);
            return new MillisecondsTimePoint(time.getTime());
        }
    }

    public static class FinishingWaitingFragment extends ScheduleFragment {

        public FinishingWaitingFragment() {

        }

        public static FinishingWaitingFragment newInstance() {
            FinishingWaitingFragment fragment = new FinishingWaitingFragment();
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.race_finish_wait, container, false);

            return layout;
        }
    }
}
