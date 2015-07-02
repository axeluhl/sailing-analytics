package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainScheduleFragment extends BaseFragment implements View.OnClickListener {

    public static final String START_TIME = "startTime";
    public static final String DEPENDENT_RACE = "dependendRace";
    private static final String TAG = MainScheduleFragment.class.getName();

    private TextView mStartTimeTextView;
    private String mStartTimeString;
    private TimePoint mStartTime;
    private TextView mWindValue;
    private RacingProcedureType mRacingProcedureType;

    private TextView mStartProcedureValue;
    private View mStartModeView;
    private TextView mStartModeTextView;
    private ImageView mStartModeImageView;
    private TextView mCourseTextView;
    private ImageView mCourseImageView;
    private SimpleDateFormat mDateFormat;
    private Calendar mCalendar;

    private RaceStateChangedListener mStateListener;

    public MainScheduleFragment() {
        mCalendar = Calendar.getInstance();
    }

    public static MainScheduleFragment newInstance() {
        MainScheduleFragment fragment = new MainScheduleFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule, container, false);

        mStateListener = new RaceStateChangedListener();
        mDateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);

        View startTime = ViewHolder.get(layout, R.id.start_time);
        if (startTime != null) {
            startTime.setOnClickListener(this);
        }
        mStartTimeTextView = ViewHolder.get(layout, R.id.start_time_value);

        View startProcedure = ViewHolder.get(layout, R.id.start_procedure);
        if (startProcedure != null) {
            startProcedure.setOnClickListener(this);
        }

        mStartProcedureValue = ViewHolder.get(layout, R.id.start_procedure_value);

        mStartModeView = ViewHolder.get(layout, R.id.start_mode);
        if (mStartModeView != null) {
            mStartModeView.setOnClickListener(this);
        }
        mStartModeTextView = ViewHolder.get(layout, R.id.start_mode_value);
        mStartModeImageView = ViewHolder.get(layout, R.id.start_mode_flag);

        View course = ViewHolder.get(layout, R.id.start_course);
        if (course != null) {
            course.setOnClickListener(this);
        }
        mCourseImageView = ViewHolder.get(layout, R.id.start_course_symbol);
        mCourseTextView = ViewHolder.get(layout, R.id.start_course_value);

        View start = ViewHolder.get(layout, R.id.start_race);
        if (start != null) {
            start.setOnClickListener(this);
        }

        View wind = ViewHolder.get(layout, R.id.wind);
        if (wind != null) {
            wind.setOnClickListener(this);
        }
        mWindValue = ViewHolder.get(layout, R.id.wind_value);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getRace() != null && getRaceState() != null) {
            getRaceState().addChangedListener(mStateListener);

            initStartTime();
            initStartMode();
            initCourse();
        }
    }

    private void initCourse() {
        if (mCourseTextView != null && getRaceState().getCourseDesign() != null) {

            String courseName = getCourseName();
            mCourseTextView.setText(courseName);
            if (mCourseImageView != null && !TextUtils.isEmpty(courseName)) {

                int resId = (courseName.toLowerCase(Locale.US).startsWith("i")) ? R.attr.course_updown_48dp : R.attr.course_triangle_48dp;
                Drawable drawable = BitmapHelper.getAttrDrawable(getActivity(), resId);
                if (drawable != null) {
                    mCourseImageView.setImageDrawable(drawable);
                }
                mCourseImageView.setVisibility(View.GONE);
            }
        }
    }

    private void initStartMode() {
        mRacingProcedureType = getRaceState().getRacingProcedure().getType();
        if (mRacingProcedureType != null) {
            if (mRacingProcedureType.equals(RacingProcedureType.RRS26)) {
                mStartModeView.setVisibility(View.VISIBLE);
                RRS26RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                Flags flag = procedure.getStartModeFlag();
                if (mStartModeTextView != null) {
                    mStartModeTextView.setText(flag.name());
                }
                if (mStartModeImageView != null) {
                    mStartModeImageView.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), flag.name(), 48));
                }
            } else {
                mStartModeView.setVisibility(View.GONE);
            }
            if (mStartProcedureValue != null) {

                mStartProcedureValue.setText(mRacingProcedureType.toString());
            }
        }
    }

    private void initStartTime() {
        TimePoint timePoint = (TimePoint) getArguments().getSerializable(START_TIME);
        RacingActivity activity = (RacingActivity) getActivity();
        if (timePoint == null && activity != null) {
            timePoint = activity.getStartTime();
        }
        if (timePoint != null) {
            if (activity != null) {
                activity.setStartTime(timePoint);
            }
            mStartTime = timePoint;
            mStartTimeString = mDateFormat.format(timePoint.asDate());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getRace() != null && getRaceState() != null) {
            getRaceState().removeChangedListener(mStateListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.start_course:
            openFragment(CourseFragment.newInstance(0, getRace()));
            break;

        case R.id.start_mode:
            openFragment(StartModeFragment.newInstance(0));
            break;

        case R.id.start_procedure:
            openFragment(StartProcedureFragment.newInstance(0));
            break;

        case R.id.start_race:
            startRace();
            break;

        case R.id.start_time:
            openFragment(StartTimeFragment.newInstance(getArguments().getSerializable(START_TIME)));
            break;

        case R.id.wind:
            openFragment(WindFragment.newInstance(0));
            break;

        default:
            ExLog.i(getActivity(), TAG, "Clicked on " + v);
        }
    }

    private void startRace() {
        RRS26RacingProcedure procedure = null;
        Flags flag = null;
        if (getRaceState().getRacingProcedure().getType().equals(RacingProcedureType.RRS26)) {
            procedure = getRaceState().getTypedRacingProcedure();
            flag = procedure.getStartModeFlag();
        }
        TimePoint now = MillisecondsTimePoint.now();
        getRaceState().setRacingProcedure(now, mRacingProcedureType);
        getRaceState().forceNewStartTime(now, mStartTime);
        if (procedure != null) {
            procedure.setStartModeFlag(MillisecondsTimePoint.now(), flag);
        }
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        if (mStartTimeTextView != null && !TextUtils.isEmpty(mStartTimeString)) {
            String startTimeValue = getString(R.string.start_time_value).replace("#TIME#", mStartTimeString)
                .replace("#COUNTDOWN#", calcCountdown(now));
            mStartTimeTextView.setText(startTimeValue);
        }

        if (mWindValue != null && getRace() != null && getRaceState() != null && getRaceState().getWindFix() != null) {
            String sensorData = getString(R.string.wind_sensor);
            Wind wind = getRaceState().getWindFix();
            sensorData = sensorData.replace("#AT#", mDateFormat.format(wind.getTimePoint().asDate()));
            sensorData = sensorData.replace("#FROM#", String.format("%.0f", wind.getFrom().getDegrees()));
            sensorData = sensorData.replace("#SPEED#", String.format("%.1f", wind.getKnots()));
            mWindValue.setText(sensorData);
        }
    }

    private String calcCountdown(TimePoint timePoint) {
        Calendar now = (Calendar) mCalendar.clone();
        now.setTime(timePoint.asDate());

        RacingActivity activity = (RacingActivity) getActivity();
        Calendar startTime = (Calendar) mCalendar.clone();
        startTime.setTime(activity.getStartTime().asDate());

        return TimeUtils.calcDuration(TimeUtils.floorTime(now), TimeUtils.floorTime(startTime));
    }

    private void openFragment(RaceFragment fragment) {
        if (fragment.getArguments() != null) {
            fragment.getArguments().putAll(getRecentArguments());
        } else {
            fragment.setArguments(getRecentArguments());
        }
        if (mStartTime != null) {
            fragment.getArguments().putSerializable(START_TIME, mStartTime);
        }
        getFragmentManager().beginTransaction().replace(R.id.racing_view_container, fragment).commitAllowingStateLoss();
    }

    private class RaceStateChangedListener extends BaseRaceStateChangedListener {
        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            super.onStatusChanged(state);

            openFragment(RaceInfoRaceFragment.newInstance());
        }
    }
}
