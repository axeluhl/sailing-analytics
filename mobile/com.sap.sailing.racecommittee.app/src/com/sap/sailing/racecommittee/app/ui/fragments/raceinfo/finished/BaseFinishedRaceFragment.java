/**
 * 
 */
package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.finished;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;
import com.sap.sailing.racecommittee.app.ui.activities.ResultsCapturingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.BasePanelFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.FinishedButtonFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.FinishedSubmitFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseRaceInfoRaceFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class BaseFinishedRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType> {

    private Calendar mCalendar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_finished, container, false);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);

        if (getView() != null) {
            replaceFragment(FinishedButtonFragment.newInstance(getArguments()), R.id.finished_panel_left);
            replaceFragment(FinishedSubmitFragment.newInstance(getArguments()), R.id.finished_panel_right);

            mCalendar = Calendar.getInstance();

            Calendar start = (Calendar) mCalendar.clone();
            start.setTime(getRaceState().getStartTime().asDate());
            Calendar startTime = floorTime(start);

            Calendar finishing = (Calendar) mCalendar.clone();
            finishing.setTime(getRaceState().getFinishingTime().asDate());
            Calendar finishingTime = floorTime(finishing);

            Calendar finished = (Calendar) mCalendar.clone();
            finished.setTime(getRaceState().getFinishedTime().asDate());
            Calendar finishedTime = floorTime(finished);

            final ImageView button = ViewHolder.get(getView(), R.id.edit_summary);
            if (button != null) {
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO
                    }
                });
            }

            TextView start_time = ViewHolder.get(getView(), R.id.race_start_time);
            if (start_time != null) {
                start_time.setText(dateFormat.format(startTime.getTime()));
            }

            TextView finish_start_time = ViewHolder.get(getView(), R.id.race_finish_begin_time);
            if (finish_start_time != null) {
                finish_start_time.setText(dateFormat.format(finishingTime.getTime()));
            }

            TextView finish_start_duration = ViewHolder.get(getView(), R.id.race_finish_begin_duration);
            if (finish_start_duration != null) {
                finish_start_duration.setText(calcDuration(startTime, finishingTime));
            }

            TextView finish_end_time = ViewHolder.get(getView(), R.id.race_finish_end_time);
            if (finish_end_time != null) {
                finish_end_time.setText(dateFormat.format(finishedTime.getTime()));
            }

            TextView finish_end_duration = ViewHolder.get(getView(), R.id.race_finish_end_duration);
            if (finish_end_duration != null) {
                finish_end_duration.setText(calcDuration(startTime, finishedTime));
            }

            TextView finish_duration = ViewHolder.get(getView(), R.id.race_finish_duration);
            if (finish_duration != null) {
                finish_duration.setText(calcDuration(finishingTime, finishedTime));
            }

            View region_wind = ViewHolder.get(getView(), R.id.region_wind);
            if (region_wind != null) {
                region_wind.setVisibility(View.GONE);
                if (getRaceState().getWindFix() != null) {
                    region_wind.setVisibility(View.VISIBLE);

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

            View region_recall = ViewHolder.get(getView(), R.id.region_individual_recalls);
            if (region_recall != null) {
                region_recall.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void setupUi() {

    }

    private String calcDuration(Calendar from, Calendar to) {
        String retValue;

        long millis = to.getTimeInMillis() - from.getTimeInMillis();

        long min = millis / (1000 * 60);
        long sec = (millis - (min * 60 * 1000)) / 1000;

        retValue = String.valueOf(sec) + "\"";
        if (retValue.length() == 2) {
            retValue = "0" + retValue;
        }
        if (min > 0) {
            retValue = String.valueOf(min) + "' " + retValue;
        }

        return retValue;
    }

    private Calendar floorTime(Calendar calendar) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }
}
