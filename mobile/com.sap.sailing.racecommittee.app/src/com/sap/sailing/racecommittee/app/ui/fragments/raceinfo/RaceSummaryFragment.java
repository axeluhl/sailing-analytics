package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.racecommittee.app.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RaceSummaryFragment extends BaseFragment {

    public static RaceSummaryFragment newInstance(Bundle args) {
        RaceSummaryFragment fragment = new RaceSummaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_summary, container, false);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);

        if (getView() != null) {
            Calendar calendar = Calendar.getInstance();

            Calendar start = (Calendar) calendar.clone();
            start.setTime(getRaceState().getStartTime().asDate());
            Calendar startTime = floorTime(start);

            Calendar finishing = (Calendar) calendar.clone();
            finishing.setTime(getRaceState().getFinishingTime().asDate());
            Calendar finishingTime = floorTime(finishing);

            Calendar finished = (Calendar) calendar.clone();
            finished.setTime(getRaceState().getFinishedTime().asDate());
            Calendar finishedTime = floorTime(finished);

            final ImageView button = ViewHolder.get(getView(), R.id.edit_summary);
            if (button != null) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Function not yet implemented.", Toast.LENGTH_SHORT).show();
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
