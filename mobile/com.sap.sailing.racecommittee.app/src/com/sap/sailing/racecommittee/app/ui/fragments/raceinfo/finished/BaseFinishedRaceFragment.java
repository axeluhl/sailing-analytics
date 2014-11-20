/**
 * 
 */
package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sap.sailing.domain.racelog.state.RaceStateChangedListener;
import com.sap.sailing.domain.racelog.state.ReadonlyRaceState;
import com.sap.sailing.domain.racelog.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.activities.ResultsCapturingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseRaceInfoRaceFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;

public abstract class BaseFinishedRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType> {

    private static String getFullRaceName(ManagedRace race) {
        return String.format("%s - %s", new BoatClassSeriesFleet(race).getDisplayName(), race.getRaceName());
    }
    
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
    
    @Override
    public void onStart() {
        super.onStart();
        getRaceState().addChangedListener(changeListener);
    }

    @Override
    public void onStop() {
        getRaceState().removeChangedListener(changeListener);
        super.onStop();
    }
    
    @Override
    protected void setupUi() {
        headerView.setText(getHeaderText());
        startTimeView.setText(getStartTimeText());
        firstBoatFinishedView.setText(getFirstBoatFinishedTimeText());
        finishTimeView.setText(getFinishTimeText());
        timeLimitView.setText(getTimeLimitText());
        protestStartTimeView.setText(getProtestStartTimeText());
    }

    private CharSequence getProtestStartTimeText() {
        TimePoint protestStartTime = getRaceState().getProtestTime();
        if (protestStartTime != null) {
            return String.format(getString(R.string.protest_start_time), TimeUtils.formatTime(protestStartTime));
        }
        return getString(R.string.empty);
    }

    protected CharSequence getTimeLimitText() {
        return getString(R.string.empty);
    }

    private CharSequence getFinishTimeText() {
        TimePoint finishTime = getRaceState().getFinishedTime();
        if (finishTime != null) {
            return String.format("%s %s", getString(R.string.race_finished_end_time), TimeUtils.formatTime(finishTime));
        }
        return getString(R.string.empty);
    }

    private CharSequence getFirstBoatFinishedTimeText() {
        TimePoint firstBoatTime = getRaceState().getFinishingTime();
        if (firstBoatTime != null) {
            return String.format("%s %s", getString(R.string.race_first_boat_finished), TimeUtils.formatTime(firstBoatTime));
        }
        return getString(R.string.empty);
    }

    private CharSequence getStartTimeText() {
        TimePoint startTime = getRaceState().getStartTime();
        if (startTime != null) {
            return String.format("%s %s", getString(R.string.race_finished_start_time), TimeUtils.formatTime(startTime));
        }
        return getString(R.string.empty);
    }

    private String getHeaderText() {
        return String.format(String.valueOf(getText(R.string.race_finished_template)), getRace().getName());
    }
    
    private RaceStateChangedListener changeListener = new BaseRaceStateChangedListener() {
        @Override
        public void onStartTimeChanged(ReadonlyRaceState state) {
            setupUi();
        }
        
        @Override
        public void onFinishingTimeChanged(ReadonlyRaceState state) {
            setupUi();
        };
        
        @Override
        public void onFinishedTimeChanged(ReadonlyRaceState state) {
            setupUi();
        };
        
        @Override
        public void onProtestTimeChanged(ReadonlyRaceState state) {
            setupUi();
        }
    };

}
