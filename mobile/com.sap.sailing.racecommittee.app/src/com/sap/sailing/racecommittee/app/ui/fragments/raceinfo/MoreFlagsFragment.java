package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.Result;
import com.sap.sailing.racecommittee.app.ui.adapters.MoreFlagsAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.MoreFlagsAdapter.MoreFlag;
import com.sap.sailing.racecommittee.app.ui.adapters.MoreFlagsAdapter.MoreFlagItemClick;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MoreFlagsFragment extends BaseFragment implements MoreFlagItemClick {

    private MoreFlagsAdapter mAdapter;

    public MoreFlagsFragment() {

    }

    public static MoreFlagsFragment newInstance() {
        MoreFlagsFragment fragment = new MoreFlagsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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
                replaceFragment(FinishTimeFragment.newInstance(0), getFrameId(getActivity(), R.id.race_edit, R.id.race_content, true));
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

    public static class FinishTimeFragment extends BaseFragment implements View.OnClickListener {

        private SimpleDateFormat mDateFormat;
        private TimePicker mTimePicker;
        private NumberPicker mSecondPicker;
        private TextView mCurrentTime;

        public FinishTimeFragment() {

        }

        public static FinishTimeFragment newInstance(int startMode) {
            FinishTimeFragment fragment = new FinishTimeFragment();
            Bundle args = new Bundle();
            args.putInt(START_MODE, startMode);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.race_finish_config, container, false);

            mDateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);

            final Calendar calendar = Calendar.getInstance();
            mTimePicker = ViewHelper.get(layout, R.id.time_picker);
            mSecondPicker = ViewHelper.get(layout, R.id.second_picker);
            TimeUtils.initTimePickerWithSeconds(getActivity(), calendar, mTimePicker, mSecondPicker);

            mCurrentTime = ViewHelper.get(layout, R.id.current_time);

            View finishCurrent = ViewHelper.get(layout, R.id.finish_current);
            if (finishCurrent != null) {
                finishCurrent.setOnClickListener(this);
            }

            View finishCustom = ViewHelper.get(layout, R.id.finish_custom);
            if (finishCustom != null) {
                finishCustom.setOnClickListener(this);
            }

            return layout;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            View header = ViewHelper.get(getView(), R.id.header_text);
            if (header != null) {
                header.setOnClickListener(this);
            }

            View back = ViewHelper.get(getView(), R.id.header_back);
            if (back != null) {
                back.setVisibility(View.VISIBLE);
            }

            switch (getArguments().getInt(START_MODE, 0)) {
                case 0: // Race-State: Running -> Start Finishing
                    if (AppUtils.with(getActivity()).isLandscape()) {
                        if (header != null) {
                            header.setOnClickListener(null);
                        }

                        if (back != null) {
                            back.setVisibility(View.GONE);
                        }
                    }
                    break;

                case 1: // Race-State: Finishing -> End Finishing
                    ImageView flag = ViewHelper.get(getView(), R.id.header_flag);
                    if (flag != null) {
                        flag.setImageDrawable(FlagsResources
                            .getFlagDrawable(getActivity(), Flags.BLUE.name(), getResources().getInteger(R.integer.flag_size)));
                    }

                    TextView headline = ViewHelper.get(getView(), R.id.header_headline);
                    if (headline != null) {
                        headline
                            .setText(getString(R.string.race_end_finish_header, mDateFormat.format(getRaceState().getFinishingTime().asMillis())));
                    }
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

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.finish_current:
                    setFinishTime(MillisecondsTimePoint.now());
                    break;

                case R.id.finish_custom:
                    setFinishTime(TimeUtils.getTime(mTimePicker, mSecondPicker));
                    break;

                default:
                    sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;
            }
        }

        @Override
        public void notifyTick(TimePoint now) {
            super.notifyTick(now);

            if (mCurrentTime != null) {
                mCurrentTime.setText(mDateFormat.format(now.asMillis()));
                mCurrentTime.setVisibility(View.VISIBLE);
            }
        }

        private void setFinishTime(TimePoint finishTime) {
            Result result = new Result();
            switch (getArguments().getInt(START_MODE, 0)) {
                case 1: // Race-State: Finishing -> End Finishing
                    if (RaceLogRaceStatus.FINISHING.equals(getRace().getStatus())) {
                        result = getRace().setFinishedTime(finishTime);
                    } else {
                        result.setError(R.string.error_wrong_race_state, RaceLogRaceStatus.FINISHING.name(), getRace().getStatus().name());
                    }
                    break;

                default: // Race-State: Running -> Start Finishing
                    if (RaceLogRaceStatus.RUNNING.equals(getRace().getStatus())) {
                        result = getRace().setFinishingTime(finishTime);
                    } else {
                        result.setError(R.string.error_wrong_race_state, RaceLogRaceStatus.RUNNING.name(), getRace().getStatus().name());
                    }
                    break;
            }

            if (result.hasError()) {
                Toast.makeText(getActivity(), result.getMessage(getActivity()), Toast.LENGTH_LONG).show();
            }
        }
    }
}
