package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import java.util.Calendar;

import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite.Resolver;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl.GateLaunchTimePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl.PathfinderPrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.impl.StartmodePrerequisite;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DatePickerFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.PrerequisiteRaceDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.RaceChooseGateLaunchTimesDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.RaceChoosePathFinderDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.RaceChooseStartModeDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.SetStartTimeRaceFragment.SetStartTimeState;
import com.sap.sailing.racecommittee.app.utils.NextFragmentListener;
import com.sap.sailing.racecommittee.app.utils.TickListener;

public class LineStartFragment extends RaceFragment implements TickListener {

    private NextFragmentListener mListener;
    private TextView mTime;
    private TimePicker mTimePicker;
    private Button mDatePicker;
    private DatePickerFragment mDatePickerFragment;

    public LineStartFragment() {
        
    }
    
    public LineStartFragment(NextFragmentListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_procedure_line_start, container, false);

        mTime = (TextView) view.findViewById(R.id.time_remaining);
        
        mDatePickerFragment = new DatePickerFragment();
        setupDatePicker();
        
        mDatePicker = (Button) view.findViewById(R.id.date);
        mDatePicker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatePickerFragment.show(getFragmentManager(), "datePicker");
            }
        });
        
        mTimePicker = (TimePicker) view.findViewById(R.id.time);
        mTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
            
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                // TODO
            }
        });
        setupTimePicker();
                
        Button header = (Button) view.findViewById(R.id.header);
        if (header != null) {
            header.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    RacingActivity activity = (RacingActivity) getActivity();
                    if (activity != null) {
                        RaceFragment fragment = new StartProcedureChangeFragment();
                        fragment.setArguments(getArguments());
                        activity.replaceFragment(fragment);
                    }
                }
            });
        }
        
        Button in1Min = (Button) view.findViewById(R.id.button_1min);
        if (in1Min != null) {
            in1Min.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    addMinutes(1);
                }
            });
        }
        
        Button in5min = (Button) view.findViewById(R.id.button_5min);
        if (in5min != null) {
            in5min.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    addMinutes(5);
                }
            });
        }
        
        Button confirm = (Button) view.findViewById(R.id.confirm);
        if (confirm != null) {
            confirm.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Save data
                    mListener.nextFragment();
                }
            });
        }

        return view;
    }
    
    private void addMinutes(int time) {
        final TimePoint now = MillisecondsTimePoint.now();
        getRaceState().setAdvancePass(now);
        getRaceState().setRacingProcedure(now, RacingProcedureType.RRS26);
        getRaceState().requestNewStartTime(now, now.plus(time * 60 * 1000), getResolver());
    }
    
    private Resolver getResolver() {
        return new RacingProcedurePrerequisite.Resolver() {
            @Override
            public void fulfill(GateLaunchTimePrerequisite prerequisite) {
                RaceDialogFragment dialog = PrerequisiteRaceDialog.setPrerequisiteArguments(
                        new RaceChooseGateLaunchTimesDialog(), getRace(), prerequisite);
                dialog.show(getFragmentManager(), "userActionRequiredDialog");
            }

            @Override
            public void fulfill(PathfinderPrerequisite prerequisite) {
                RaceDialogFragment dialog = PrerequisiteRaceDialog.setPrerequisiteArguments(
                        new RaceChoosePathFinderDialog(), getRace(), prerequisite);
                dialog.show(getFragmentManager(), "userActionRequiredDialog");
            }

            @Override
            public void fulfill(StartmodePrerequisite prerequisite) {
                RaceDialogFragment dialog = PrerequisiteRaceDialog.setPrerequisiteArguments(
                        new RaceChooseStartModeDialog(), getRace(), prerequisite);
                dialog.show(getFragmentManager(), "userActionRequiredDialog");
            }

            @Override
            public void onFulfilled() {
                // nothing
            }
        };
    }
    
    @Override
    public void notifyTick() {
        if (mTime != null && getRaceState().getStartTime() != null) {
          mTime.setText(getDuration(getRaceState().getStartTime().asDate(), Calendar.getInstance().getTime()));
        }
        
        super.notifyTick();
    }
    
    private void setupTimePicker() {
        mTimePicker.setIs24HourView(true);

        // In 10 minutes from now, but always a 5-minute-mark.
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 10);
        int hours = now.get(Calendar.HOUR_OF_DAY);
        int minutes = now.get(Calendar.MINUTE);
        minutes = (int) (Math.ceil((minutes / 5.0)) * 5.0);
        if (minutes >= 60) {
            hours++;
        }

        mTimePicker.setCurrentHour(hours);
        mTimePicker.setCurrentMinute(minutes);
    }
    
    private void setupDatePicker() {
        mDatePickerFragment.setOnDateSetListener(new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar startDate = Calendar.getInstance();
                startDate.set(year, month, day);
                Calendar today = Calendar.getInstance();

                if (startDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                        && startDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    mDatePicker.setText(R.string.today);
                } else {
                    String dateString = DateFormat.getDateFormat(getActivity()).format(startDate.getTime());
                    mDatePicker.setText(dateString);
                }
                mDatePicker.setTag(startDate);
                
            }
        });
        final Calendar today = Calendar.getInstance();
        mDatePickerFragment.setCurrentYear(today.get(Calendar.YEAR));
        mDatePickerFragment.setCurrentMonth(today.get(Calendar.MONTH));
        mDatePickerFragment.setCurrentDay(today.get(Calendar.DAY_OF_MONTH));
    }
}
