package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import java.util.Calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderStatus;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceFinishingTimeDialog extends RaceDialogFragment {

    private TimePicker timePicker;

    private void setupTimePicker(TimePicker timePicker) {
        timePicker.setIs24HourView(true);
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        timePicker.setCurrentHour(hours);
        timePicker.setCurrentMinute(minutes);
    }

    private void setAndAnnounceFinishedTime() {
        TimePoint finishingTime = getFinishingTime();
        StartTimeFinder stf = new StartTimeFinder(getRace().getRaceLog());
        
        Pair<StartTimeFinderStatus, TimePoint> startTimeFinderResult = stf.analyze();
        if (startTimeFinderResult.getA().equals(StartTimeFinderStatus.STARTTIME_UNKNOWN)){
            return;
        }
        
        TimePoint startTime;
        if (startTimeFinderResult.getA().equals(StartTimeFinderStatus.STARTTIME_FOUND)){
            startTime = startTimeFinderResult.getB();
        } else { //DependentStartTime
            //FIXME: bug2467 fetch dependentStartTime via DependentStartTimeAnalyzer
            startTime = MillisecondsTimePoint.now();
        }
        
        if (getRace().getStatus().equals(RaceLogRaceStatus.RUNNING)) {
            if (startTime.before(finishingTime)) {
                getRace().getState().setFinishingTime(finishingTime);
                dismiss();
            }else{
                Toast.makeText(getActivity(), "The selected time is before the race start.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private TimePoint getFinishingTime() {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        time.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        return new MillisecondsTimePoint(time.getTime());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.finished_time_view, null);
        timePicker = (TimePicker) view.findViewById(R.id.protest_time_time_time_picker);
        setupTimePicker(timePicker);
        getDialog().setTitle(getText(R.string.finished_dialog_title));
        Button chooseButton = (Button) view.findViewById(R.id.chooseFinishedTimeButton);
        chooseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setAndAnnounceFinishedTime();
            }
        });

        return view;

    }
}