package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainScheduleFragment extends BaseFragment implements View.OnClickListener {

    public static final String STARTTIME = "StartTime";

    private static final String TAG = MainScheduleFragment.class.getName();

    private TextView mStartTime;
    private String mStartTimeString;
    private TimePoint mProtestTime;
    private TextView mWindValue;
    private RacingProcedureType mRacingProcedureType;

    private TextView mStartProcedureValue;
    private View mStartMode;
    private TextView mStartModeValue;
    private ImageView mStartModeFlag;
    private TextView mCourseValue;
    private ImageView mCourseSymbol;
    private SimpleDateFormat mDateFormat;
    private Calendar mCalendar;

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

        mDateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);

        View startTime = ViewHolder.get(layout, R.id.start_time);
        if (startTime != null) {
            startTime.setOnClickListener(this);
        }
        mStartTime = ViewHolder.get(layout, R.id.start_time_value);

        View startProcedure = ViewHolder.get(layout, R.id.start_procedure);
        if (startProcedure != null) {
            startProcedure.setOnClickListener(this);
        }

        mStartProcedureValue = ViewHolder.get(layout, R.id.start_procedure_value);

        mStartMode = ViewHolder.get(layout, R.id.start_mode);
        if (mStartMode != null) {
            mStartMode.setOnClickListener(this);
        }
        mStartModeValue = ViewHolder.get(layout, R.id.start_mode_value);
        mStartModeFlag = ViewHolder.get(layout, R.id.start_mode_flag);

        View course = ViewHolder.get(layout, R.id.start_course);
        if (course != null) {
            course.setOnClickListener(this);
        }
        mCourseSymbol = ViewHolder.get(layout, R.id.start_course_symbol);
        mCourseValue = ViewHolder.get(layout, R.id.start_course_value);

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();

        TickSingleton.INSTANCE.registerListener(this);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);
        if (getRace() != null) {
            if (getRaceState() != null) {
                TimePoint timePoint = (TimePoint) getArguments().getSerializable(STARTTIME);
                RacingActivity activity = (RacingActivity) getActivity();
                if (timePoint == null && activity != null) {
                    timePoint = activity.getStartTime();
                }
                if (timePoint != null) {
                    if (activity != null) {
                        activity.setStartTime(timePoint);
                    }
                    mProtestTime = timePoint;
                    mStartTimeString = dateFormat.format(timePoint.asDate());
                }

                mRacingProcedureType = getRaceState().getRacingProcedure().getType();
                if (getRaceState().getRacingProcedure().getType().equals(RacingProcedureType.RRS26)) {
                    mStartMode.setVisibility(View.VISIBLE);
                    RRS26RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                    Flags flag = procedure.getStartModeFlag();
                    if (mStartModeValue != null) {
                        mStartModeValue.setText(flag.name());
                    }
                    if (mStartModeFlag != null) {
                        mStartModeFlag.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), flag.name(), 48));
                    }
                } else {
                    mStartMode.setVisibility(View.GONE);
                }
                if (mStartProcedureValue != null) {
                    if (getRaceState().getRacingProcedure().getType() != null) {
                        mStartProcedureValue.setText(getRaceState().getRacingProcedure().getType().toString());
                    }
                }

                if (mCourseValue != null) {
                    if (getRaceState().getCourseDesign() != null) {
                        String courseName = getRaceState().getCourseDesign().getName();
                        mCourseValue.setText(courseName);
                        if (mCourseSymbol != null && !TextUtils.isEmpty(courseName)) {
                            int resId;
                            if (courseName.toLowerCase().startsWith("i")) {
                                resId = R.attr.course_updown_48dp;
                            } else {
                                resId = R.attr.course_triangle_48dp;
                            }
                            Drawable drawable;
                            drawable = BitmapHelper.getAttrDrawable(getActivity(), resId);
                            if (drawable != null) {
                                mCourseSymbol.setImageDrawable(drawable);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        TickSingleton.INSTANCE.unregisterListener(this);
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
                RRS26RacingProcedure procedure = null;
                Flags flag = null;
                if (getRaceState().getRacingProcedure().getType().equals(RacingProcedureType.RRS26)) {
                    procedure = getRaceState().getTypedRacingProcedure();
                    flag = procedure.getStartModeFlag();
                }
                TimePoint now = MillisecondsTimePoint.now();
                getRaceState().setAdvancePass(now);
                getRaceState().setRacingProcedure(now, mRacingProcedureType);
                getRaceState().forceNewStartTime(now, mProtestTime);
                if (procedure != null) {
                    procedure.setStartModeFlag(MillisecondsTimePoint.now(), flag);
                }
                openFragment(RaceInfoRaceFragment.newInstance());
                break;

            case R.id.start_time:
                openFragment(StartTimeFragment.newInstance(0));
                break;

            case R.id.wind:
                openFragment(WindFragment.newInstance(0));
                break;

            default:
                Toast.makeText(getActivity(), "Clicked on " + v, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void notifyTick() {
        super.notifyTick();

        if (mStartTime != null && !TextUtils.isEmpty(mStartTimeString)) {
            String startTimeValue = getString(R.string.start_time_value).replace("#TIME#", mStartTimeString).replace("#COUNTDOWN#", calcCountdown());
            mStartTime.setText(startTimeValue);
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

    private String calcCountdown() {
        Calendar now = (Calendar) mCalendar.clone();
        now.setTime(MillisecondsTimePoint.now().asDate());

        RacingActivity activity = (RacingActivity) getActivity();
        Calendar startTime = (Calendar) mCalendar.clone();
        startTime.setTime(activity.getStartTime().asDate());

        return calcDuration(floorTime(now), floorTime(startTime));
    }

    private void openFragment(RaceFragment fragment) {
        fragment.setArguments(getRecentArguments());
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.racing_view_container, fragment)
                .commitAllowingStateLoss();
    }
}
