package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.chooser.RaceInfoFragmentChooser;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.text.SimpleDateFormat;

public class MainScheduleFragment extends RaceFragment implements View.OnClickListener {

    private static final String TAG = MainScheduleFragment.class.getName();

    private TextView mStartTime;
    private String mStartTimeString;
    private TimePoint mProtestTime;
    private RacingProcedureType mRacingProcedureType;

    private TextView mStartProcedureValue;
    private View mStartMode;
    private TextView mStartModeValue;
    private ImageView mStartModeFlag;

    public static MainScheduleFragment newInstance() {
        MainScheduleFragment fragment = new MainScheduleFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule, container, false);

        View startTime = view.findViewById(R.id.start_time);
        if (startTime != null) {
            startTime.setOnClickListener(this);
        }
        mStartTime = (TextView) view.findViewById(R.id.start_time_value);

        View startProcedure = view.findViewById(R.id.start_procedure);
        if (startProcedure != null) {
            startProcedure.setOnClickListener(this);
        }

        mStartProcedureValue = (TextView) view.findViewById(R.id.start_procedure_value);

        mStartMode = view.findViewById(R.id.start_mode);
        if (mStartMode != null) {
            mStartMode.setOnClickListener(this);
        }
        mStartModeValue = (TextView) view.findViewById(R.id.start_mode_value);
        mStartModeFlag = (ImageView) view.findViewById(R.id.start_mode_flag);

        View course = view.findViewById(R.id.start_course);
        if (course != null) {
            course.setOnClickListener(this);
        }

        View start = view.findViewById(R.id.start_race);
        if (start != null) {
            start.setOnClickListener(this);
        }

        View wind = view.findViewById(R.id.wind);
        if (wind != null) {
            wind.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        TickSingleton.INSTANCE.registerListener(this);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);
        if (getRace() != null) {
            if (getRaceState() != null) {
                TimePoint timePoint = getRaceState().getProtestTime();
                if (timePoint != null && mStartTime != null) {
                    mProtestTime = timePoint;
                    mStartTimeString = simpleDateFormat.format(timePoint.asDate());
                }
                mRacingProcedureType = getRaceState().getRacingProcedure().getType();
                RRS26RacingProcedure procedure = null;
                mStartMode.setVisibility(View.VISIBLE);
                try {
                    procedure = (RRS26RacingProcedure) getRaceState().getRacingProcedure();
                } catch (ClassCastException ex) {
                    mStartMode.setVisibility(View.GONE);
                }
                if (procedure != null) {
                    Flags flag = procedure.getStartModeFlag();
                    if (mStartModeValue != null) {
                        mStartModeValue.setText(flag.name());
                    }
                    if (mStartModeFlag != null) {
                        mStartModeFlag.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), flag.name(), 1));
                    }
                }
                if (mStartProcedureValue != null) {
                    mStartProcedureValue.setText(getRaceState().getRacingProcedure().getType().name());
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
            case R.id.start_mode:
                openFragment(StartModeFragment.newInstance());
                break;

            case R.id.start_procedure:
                openFragment(StartProcedureFragment.newInstance());
                break;

            case R.id.start_race:
                TimePoint now = MillisecondsTimePoint.now();
                getRaceState().setAdvancePass(now);
                getRaceState().setRacingProcedure(now, mRacingProcedureType);
                getRaceState().forceNewStartTime(now, mProtestTime);
                RaceInfoFragmentChooser fragmentChooser = RaceInfoFragmentChooser.on(mRacingProcedureType);
                openFragment(fragmentChooser.getStartFragment(getActivity(), getRace()));
                break;

            case R.id.start_time:
                openFragment(StartTimeFragment.newInstance(true));
                break;

            case R.id.wind:
                openFragment(WindFragment.newInstance());
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
    }

    private void openFragment(RaceFragment fragment) {
        fragment.setArguments(getRecentArguments());
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.racing_view_container, fragment)
                .commit();
    }
}
