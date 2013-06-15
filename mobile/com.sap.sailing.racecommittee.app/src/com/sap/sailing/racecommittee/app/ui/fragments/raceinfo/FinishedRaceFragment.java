/**
 * 
 */
package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.activities.ResultsCapturingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.PositioningFragment;

public class FinishedRaceFragment extends RaceFragment {

    TextView headerView;
    TextView startTimeView;
    TextView firstBoatFinishedView;
    TextView finishTimeView;
    TextView timeLimitView;
    TextView protestStartTimeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_finished_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        headerView = (TextView) getView().findViewById(R.id.textFinishedRace);
        startTimeView = (TextView) getView().findViewById(R.id.textFinishedRaceStarted);
        firstBoatFinishedView = (TextView) getView().findViewById(R.id.textFirstBoatFinished);
        finishTimeView = (TextView) getView().findViewById(R.id.textFinishedRaceEnded);
        timeLimitView = (TextView) getView().findViewById(R.id.textTimeLimit);
        protestStartTimeView = (TextView) getView().findViewById(R.id.textProtestStartTime);

        headerView.setText(getHeaderText());
        startTimeView.setText(getStartTimeText());
        firstBoatFinishedView.setText(getFirstBoatFinishedTimeText());
        finishTimeView.setText(getFinishTimeText());
        timeLimitView.setText(getTimeLimitText());
        updateProtestStartTimeLabel();

        Button positioningButton = (Button) getView().findViewById(R.id.buttonPositioning);
        positioningButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PositioningFragment fragment = new PositioningFragment();
                fragment.setArguments(PositioningFragment.createArguments(getRace()));
                fragment.show(getFragmentManager(), null);
            }
        });

        ImageButton cameraButton = (ImageButton) getView().findViewById(R.id.buttonCamera);
        cameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ResultsCapturingActivity.createIntent(view.getContext(),
                        String.format(getString(R.string.results_mail_subject), getFullRaceName(getRace())),
                        String.format(getString(R.string.results_mail_body), getFullRaceName(getRace()))));
            }
        });
    }

    private void updateProtestStartTimeLabel() {
        protestStartTimeView.setText(getProtestStartTimeText());
    }

    @Override
    public void onStart() {
        super.onStart();
        ExLog.i(FinishedRaceFragment.class.getName(),
                String.format("Fragment %s is now shown", FinishedRaceFragment.class.getName()));

        getRace().getState().registerStateChangeListener(stateListener);
    }

    @Override
    public void onStop() {
        getRace().getState().unregisterStateChangeListener(stateListener);
        super.onStop();
    }

    private CharSequence getProtestStartTimeText() {
        TimePoint protestStartTime = getRace().getState().getProtestStartTime();
        if (protestStartTime != null) {
            return String.format(getString(R.string.protest_start_time), getFormattedTime(protestStartTime.asDate()));
        }
        return getString(R.string.empty);
    }

    private TimePoint getTimeLimit() {
        TimePoint startTime = getRace().getState().getStartTime();
        TimePoint firstBoatTime = getRace().getState().getFinishingStartTime();
        if (startTime == null || firstBoatTime == null) {
            return null;
        }
        return firstBoatTime.plus((long) ((firstBoatTime.asMillis() - startTime.asMillis()) * 0.75));
    }

    private CharSequence getTimeLimitText() {
        TimePoint timeLimit = getTimeLimit();
        if (timeLimit != null) {
            return String.format(getString(R.string.race_time_limit), getFormattedTime(timeLimit.asDate()));
        }
        return getString(R.string.empty);
    }

    private CharSequence getFinishTimeText() {
        TimePoint finishTime = getRace().getState().getFinishedTime();
        if (finishTime != null) {
            return String.format("%s %s", getString(R.string.race_finished_end_time),
                    getFormattedTime(finishTime.asDate()));
        }
        return getString(R.string.empty);
    }

    private CharSequence getFirstBoatFinishedTimeText() {
        TimePoint firstBoatTime = getRace().getState().getFinishingStartTime();
        if (firstBoatTime != null) {
            return String.format("%s %s", getString(R.string.race_first_boat_finished),
                    getFormattedTime(firstBoatTime.asDate()));
        }
        return getString(R.string.empty);
    }

    private CharSequence getStartTimeText() {
        TimePoint startTime = getRace().getState().getStartTime();
        if (startTime != null) {
            return String.format("%s %s", getString(R.string.race_finished_start_time),
                    getFormattedTime(startTime.asDate()));
        }
        return getString(R.string.empty);
    }

    private String getHeaderText() {
        return String.format(String.valueOf(getText(R.string.race_finished_template)), getRace().getName());
    }

    private String getFormattedTime(Date time) {
        return getFormattedTimePart(time.getHours()) + ":" + getFormattedTimePart(time.getMinutes()) + ":"
                + getFormattedTimePart(time.getSeconds());
    }

    private String getFormattedTimePart(int timePart) {
        return (timePart < 10) ? "0" + timePart : String.valueOf(timePart);
    }

    private RaceStateChangedListener stateListener = new RaceStateChangedListener() {

        @Override
        public void onRaceStateProtestStartTimeChanged(RaceState state) {
            if (getRace().getState().equals(state)) {
                updateProtestStartTimeLabel();
            }
        }

        @Override
        public void onRaceStateCourseDesignChanged(RaceState state) {
            // not my business
        }

        @Override
        public void onRaceStateStatusChanged(RaceState state) {
            // not my business
        }
    };
    
    private static String getFullRaceName(ManagedRace race) {
        return String.format("%s - %s", new BoatClassSeriesFleet(race).getDisplayName(), race.getRaceName());
    }

}
