package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.text.SimpleDateFormat;

public class MainScheduleFragment extends RaceFragment implements View.OnClickListener {

    public static final String STARTTIME ="StartTime";

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

    public static MainScheduleFragment newInstance() {
        MainScheduleFragment fragment = new MainScheduleFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule, container, false);

        View startTime = layout.findViewById(R.id.start_time);
        if (startTime != null) {
            startTime.setOnClickListener(this);
        }
        mStartTime = (TextView) layout.findViewById(R.id.start_time_value);

        View startProcedure = layout.findViewById(R.id.start_procedure);
        if (startProcedure != null) {
            startProcedure.setOnClickListener(this);
        }

        mStartProcedureValue = (TextView) layout.findViewById(R.id.start_procedure_value);

        mStartMode = layout.findViewById(R.id.start_mode);
        if (mStartMode != null) {
            mStartMode.setOnClickListener(this);
        }
        mStartModeValue = (TextView) layout.findViewById(R.id.start_mode_value);
        mStartModeFlag = (ImageView) layout.findViewById(R.id.start_mode_flag);

        View course = layout.findViewById(R.id.start_course);
        if (course != null) {
            course.setOnClickListener(this);
        }
        mCourseValue = (TextView) layout.findViewById(R.id.start_course_value);

        View start = layout.findViewById(R.id.start_race);
        if (start != null) {
            start.setOnClickListener(this);
        }

        View wind = layout.findViewById(R.id.wind);
        if (wind != null) {
            wind.setOnClickListener(this);
        }
        mWindValue = (TextView) layout.findViewById(R.id.wind_value);

        return layout;
    }

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
                    mStartProcedureValue.setText(getRaceState().getRacingProcedure().getType()==null?"":getRaceState().getRacingProcedure().getType().toString());
                }

                if (mCourseValue != null) {
                    mCourseValue.setText(getRaceState().getCourseDesign()==null?"":getRaceState().getCourseDesign().getName());
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
                openFragment(CourseFragment.newInstance(0));
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
            mStartTime.setText(mStartTimeString);
        }

        if (mWindValue != null && getRace() != null && getRaceState() != null && getRaceState().getWindFix() != null) {
            String sensorData = getString(R.string.wind_sensor);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);
            Wind wind = getRaceState().getWindFix();
            sensorData = sensorData.replace("#AT#", dateFormat.format(wind.getTimePoint().asDate()));
            sensorData = sensorData.replace("#FROM#", String.format("%.0f", wind.getFrom().getDegrees()));
            sensorData = sensorData.replace("#SPEED#", String.format("%.1f", wind.getKnots()));
            mWindValue.setText(sensorData);
        }
    }

    private void openFragment(RaceFragment fragment) {
        fragment.setArguments(getRecentArguments());
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.racing_view_container, fragment)
                .commitAllowingStateLoss();
    }
}
