package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceInfoFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.chooser.RaceInfoFragmentChooser;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sse.common.TimePoint;

import java.text.SimpleDateFormat;

public class MainScheduleFragment extends RaceFragment implements View.OnClickListener {

    private static final String TAG = MainScheduleFragment.class.getName();

    private TextView mStartTime;
    private String mStartTimeString;

    public static MainScheduleFragment newInstance() {
        MainScheduleFragment fragment = new MainScheduleFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule, container, false);

        RelativeLayout startTime = (RelativeLayout) view.findViewById(R.id.start_time);
        if (startTime != null) {
            startTime.setOnClickListener(this);
        }
        mStartTime = (TextView) view.findViewById(R.id.start_time_value);

        RelativeLayout startProcedure = (RelativeLayout) view.findViewById(R.id.start_procedure);
        if (startProcedure != null) {
            startProcedure.setOnClickListener(this);
        }

        RelativeLayout startMode = (RelativeLayout) view.findViewById(R.id.start_mode);
        if (startMode != null) {
            startMode.setOnClickListener(this);
        }

        RelativeLayout course = (RelativeLayout) view.findViewById(R.id.start_course);
        if (course != null) {
            course.setOnClickListener(this);
        }

        Button start = (Button) view.findViewById(R.id.start_race);
        if (start != null) {
            start.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        if (getRace() != null) {
            if (getRace().getState() != null) {
                TimePoint timePoint = getRace().getState().getProtestTime();
                if (timePoint != null && mStartTime != null) {
                    mStartTimeString = simpleDateFormat.format(timePoint.asDate());
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        TickSingleton.INSTANCE.registerListener(this);
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
                break;

            case R.id.start_procedure:
                break;

            case R.id.start_race:
                RaceInfoFragmentChooser fragmentChooser = RaceInfoFragmentChooser.on(getRace().getState().getRacingProcedure().getType());
                openFragment(fragmentChooser.getStartFragment(getActivity(), getRace()));
                break;

            case R.id.start_time:
                openFragment(StartTimeFragment.newInstance(true));
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
        fragment.setArguments(RaceFragment.createArguments(getRace()));
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.racing_view_container, fragment)
                .commit();
    }
}
