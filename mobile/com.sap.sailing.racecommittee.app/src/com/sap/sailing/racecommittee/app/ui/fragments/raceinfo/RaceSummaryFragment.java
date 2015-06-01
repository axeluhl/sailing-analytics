package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;

public class RaceSummaryFragment extends BaseFragment {

    private RaceStateListener mRaceStateListener;
    SimpleDateFormat mDateFormat;
    TextView mStartTime;
    TextView mFinishStartTime;
    TextView mFinishStartDuration;
    TextView mFinishEndTime;
    TextView mFinishEndDuration;
    TextView mFinishDuration;
    View mRegionWind;
    View mRegionRecall;

    public static RaceSummaryFragment newInstance(Bundle args) {
        RaceSummaryFragment fragment = new RaceSummaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_summary, container, false);

        mRaceStateListener = new RaceStateListener();
        mDateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);

        final ImageView button = ViewHolder.get(layout, R.id.edit_summary);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Function not yet implemented.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        mStartTime = ViewHolder.get(layout, R.id.race_start_time);
        mFinishStartTime = ViewHolder.get(layout, R.id.race_finish_begin_time);
        mFinishStartDuration = ViewHolder.get(layout, R.id.race_finish_begin_duration);
        mFinishEndTime = ViewHolder.get(layout, R.id.race_finish_end_time);
        mFinishEndDuration = ViewHolder.get(layout, R.id.race_finish_end_duration);
        mFinishDuration = ViewHolder.get(layout, R.id.race_finish_duration);
        mRegionWind = ViewHolder.get(layout, R.id.region_wind);
        mRegionRecall = ViewHolder.get(layout, R.id.region_individual_recalls);

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
        start.setTime(getRaceState().getStartTime().asDate());
        Calendar startTime = TimeUtils.floorTime(start);

        Calendar finishing = (Calendar) calendar.clone();
        finishing.setTime(getRaceState().getFinishingTime().asDate());
        Calendar finishingTime = TimeUtils.floorTime(finishing);

        Calendar finished = (Calendar) calendar.clone();
        finished.setTime(getRaceState().getFinishedTime().asDate());

        Calendar finishedTime = TimeUtils.floorTime(finished);

        if (mStartTime != null) {
            mStartTime.setText(mDateFormat.format(startTime.getTime()));
        }

        if (mFinishStartTime != null) {
            mFinishStartTime.setText(mDateFormat.format(finishingTime.getTime()));
        }

        if (mFinishStartDuration != null) {
            mFinishStartDuration.setText(TimeUtils.calcDuration(startTime, finishingTime));
        }

        if (mFinishEndTime != null) {
            mFinishEndTime.setText(mDateFormat.format(finishedTime.getTime()));
        }

        if (mFinishEndDuration != null) {
            mFinishEndDuration.setText(TimeUtils.calcDuration(startTime, finishedTime));
        }

        if (mFinishDuration != null) {
            mFinishDuration.setText(TimeUtils.calcDuration(finishingTime, finishedTime));
        }

        if (mRegionWind != null) {
            mRegionWind.setVisibility(View.GONE);
            if (getRaceState().getWindFix() != null) {
                mRegionWind.setVisibility(View.VISIBLE);

                Wind wind = getRaceState().getWindFix();

                TextView direction = ViewHolder.get(getView(), R.id.wind_direction);
                if (direction != null) {
                    String wind_direction = String.format(getString(R.string.race_summary_wind_direction_value), wind.getFrom().getDegrees());
                    direction.setText(wind_direction);
                }

                TextView speed = ViewHolder.get(getView(), R.id.wind_speed);
                if (speed != null) {
                    String wind_speed = String.format(getString(R.string.race_summary_wind_speed_value), wind.getKnots());
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
