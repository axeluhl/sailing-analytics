package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimeRange;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RaceSummaryFragment extends BaseFragment {

    private RaceStateListener mRaceStateListener;
    private SimpleDateFormat mDateFormat;
    private TextView mStartTime;
    private TextView mFinishStartTime;
    private TextView mFinishStartDuration;
    private TextView mFinishEndTime;
    private TextView mFinishEndDuration;
    private TextView mFinishDuration;
    private View mRegionProtest;
    private TextView mProtestTimeStart;
    private TextView mProtestTimeEnd;
    private TextView mProtestTimeDuration;
    private View mRegionWind;
    private View mRegionRecall;

    public static RaceSummaryFragment newInstance(Bundle args) {
        RaceSummaryFragment fragment = new RaceSummaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final AppUtils appUtils = AppUtils.with(inflater.getContext());
        final int resId;
        if (appUtils.isPhone() && getResources().getConfiguration().fontScale > 1 && !appUtils.isHDPI()) {
            resId = R.layout.race_summary_large_font;
        } else {
            resId = R.layout.race_summary_normal;
        }
        View layout = inflater.inflate(resId, container, false);

        mRaceStateListener = new RaceStateListener();
        mDateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);

        mStartTime = ViewHelper.get(layout, R.id.race_start_time);
        mFinishStartTime = ViewHelper.get(layout, R.id.race_finish_begin_time);
        mFinishStartDuration = ViewHelper.get(layout, R.id.race_finish_begin_duration);
        mFinishEndTime = ViewHelper.get(layout, R.id.race_finish_end_time);
        mFinishEndDuration = ViewHelper.get(layout, R.id.race_finish_end_duration);
        mFinishDuration = ViewHelper.get(layout, R.id.race_finish_duration);
        mRegionProtest = ViewHelper.get(layout, R.id.region_protest);
        mProtestTimeStart = ViewHelper.get(layout, R.id.protest_time_start);
        mProtestTimeEnd = ViewHelper.get(layout, R.id.protest_time_end);
        mProtestTimeDuration = ViewHelper.get(layout, R.id.protest_time_duration);
        mRegionWind = ViewHelper.get(layout, R.id.region_wind);
        mRegionRecall = ViewHelper.get(layout, R.id.region_individual_recalls);

        View editStartTime = ViewHelper.get(layout, R.id.edit_race_start_time);
        if (editStartTime != null) {
            editStartTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(RaceTimeChangeFragment.newInstance(RaceTimeChangeFragment.START_TIME_MODE),
                            getFrameId(getActivity(), R.id.finished_edit, R.id.finished_content, true));
                }
            });
        }

        View editFinishingTime = ViewHelper.get(layout, R.id.edit_race_finish_begin_time);
        if (editFinishingTime != null) {
            editFinishingTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(RaceTimeChangeFragment.newInstance(RaceTimeChangeFragment.FINISHING_TIME_MODE),
                            getFrameId(getActivity(), R.id.finished_edit, R.id.finished_content, true));
                }
            });
        }

        View editFinishedTime = ViewHelper.get(layout, R.id.edit_race_finish_end_time);
        if (editFinishedTime != null) {
            editFinishedTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(RaceTimeChangeFragment.newInstance(RaceTimeChangeFragment.FINISHED_TIME_MODE),
                            getFrameId(getActivity(), R.id.finished_edit, R.id.finished_content, true));
                }
            });
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showData();
    }

    @Override
    public void onResume() {
        super.onResume();

        getRaceState().addChangedListener(mRaceStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        getRaceState().removeChangedListener(mRaceStateListener);
    }

    private void showData() {
        Calendar calendar = Calendar.getInstance();
        Calendar start = (Calendar) calendar.clone();
        if (getRaceState().getStartTime() != null) {
            start.setTime(getRaceState().getStartTime().asDate());
        }
        Calendar startTime = TimeUtils.floorTime(start);
        Calendar finishing = (Calendar) calendar.clone();
        if (getRaceState().getFinishingTime() != null) {
            finishing.setTime(getRaceState().getFinishingTime().asDate());
        }
        Calendar finishingTime = TimeUtils.floorTime(finishing);
        Calendar finished = (Calendar) calendar.clone();
        if (getRaceState().getFinishedTime() != null) {
            finished.setTime(getRaceState().getFinishedTime().asDate());
        }
        Calendar finishedTime = TimeUtils.floorTime(finished);
        if (mStartTime != null && getRaceState().getStartTime() != null) {
            mStartTime.setText(mDateFormat.format(startTime.getTime()));
        }
        if (mFinishStartTime != null && getRaceState().getFinishingTime() != null) {
            mFinishStartTime.setText(mDateFormat.format(finishingTime.getTime()));
        }
        if (mFinishStartDuration != null && getRaceState().getStartTime() != null
                && getRaceState().getFinishingTime() != null) {
            mFinishStartDuration.setText(TimeUtils.formatTimeAgo(getActivity(),
                    finishingTime.getTimeInMillis() - startTime.getTimeInMillis()));
        }
        if (mFinishEndTime != null && getRaceState().getFinishedTime() != null) {
            mFinishEndTime.setText(mDateFormat.format(finishedTime.getTime()));
        }
        if (mFinishEndDuration != null && getRaceState().getStartTime() != null
                && getRaceState().getFinishedTime() != null) {
            mFinishEndDuration.setText(TimeUtils.formatTimeAgo(getActivity(),
                    finishedTime.getTimeInMillis() - startTime.getTimeInMillis()));
        }
        if (mFinishDuration != null) {
            mFinishDuration.setText(TimeUtils.formatTimeAgo(getActivity(),
                    finishedTime.getTimeInMillis() - finishingTime.getTimeInMillis()));
        }

        if (mRegionProtest != null) {
            mRegionProtest.setVisibility(View.GONE);
            if (getRaceState().getProtestTime() != null) {
                mRegionProtest.setVisibility(View.VISIBLE);

                TimeRange protestTime = getRaceState().getProtestTime();
                if (mProtestTimeStart != null) {
                    mProtestTimeStart.setText(mDateFormat.format(protestTime.from().asDate()));
                }
                if (mProtestTimeEnd != null) {
                    mProtestTimeEnd.setText(mDateFormat.format(protestTime.to().asDate()));
                }
                if (mProtestTimeDuration != null) {
                    mProtestTimeDuration.setText(TimeUtils.formatTimeAgo(getActivity(),
                            protestTime.to().minus(protestTime.from().asMillis()).asMillis()));
                }
            }
        }

        if (mRegionWind != null) {
            mRegionWind.setVisibility(View.GONE);
            if (getRaceState().getWindFix() != null) {
                mRegionWind.setVisibility(View.VISIBLE);

                Wind wind = getRaceState().getWindFix();

                TextView direction = ViewHelper.get(getView(), R.id.wind_direction);
                if (direction != null) {
                    String wind_direction = String.format(getString(R.string.race_summary_wind_direction_value),
                            wind.getFrom().getDegrees());
                    direction.setText(wind_direction);
                }

                TextView speed = ViewHelper.get(getView(), R.id.wind_speed);
                if (speed != null) {
                    String wind_speed = String.format(getString(R.string.race_summary_wind_speed_value),
                            wind.getKnots());
                    speed.setText(wind_speed);
                }
            }
        }

        if (mRegionRecall != null) {
            mRegionRecall.setVisibility(View.GONE);
        }
    }

    private class RaceStateListener implements RaceStateChangedListener {

        @Override
        public void onRacingProcedureChanged(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onStartTimeChanged(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onFinishingTimeChanged(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onFinishedTimeChanged(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onProtestTimeChanged(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onAdvancePass(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onFinishingPositioningsChanged(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onFinishingPositionsConfirmed(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onCourseDesignChanged(ReadonlyRaceState state) {
            showData();
        }

        @Override
        public void onWindFixChanged(ReadonlyRaceState state) {
            showData();
        }
    }

}
