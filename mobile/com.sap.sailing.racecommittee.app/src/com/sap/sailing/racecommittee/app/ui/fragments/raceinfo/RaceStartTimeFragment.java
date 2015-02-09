package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

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

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite.Resolver;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl.GateLaunchTimePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl.PathfinderPrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.impl.StartmodePrerequisite;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DatePickerFragment;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceStartTimeFragment extends RaceFragment {

    private static final String TAG = RaceStartTimeFragment.class.getName();
    
    private Button mDatePicker;
    private DatePickerFragment mDatePickerFragment;
    private TextView mTime;
    private TimePicker mTimePicker;

    public RaceStartTimeFragment() {
        
    }
    
    private void addMinutes(int time) {
        final TimePoint now = MillisecondsTimePoint.now();
        getRaceState().setAdvancePass(now);
        getRaceState().requestNewStartTime(now, now.plus(time * 60 * 1000), getResolver());
    }
    
    private Resolver getResolver() {
        return new RacingProcedurePrerequisite.Resolver() {
            @Override
            public void fulfill(GateLaunchTimePrerequisite prerequisite) {
                ExLog.i(getActivity(), TAG, "GateLaunchTimePrerequisite");
//                RaceDialogFragment dialog = PrerequisiteRaceDialog.setPrerequisiteArguments(
//                        new RaceChooseGateLaunchTimesDialog(), getRace(), prerequisite);
//                dialog.show(getFragmentManager(), "userActionRequiredDialog");
            }

            @Override
            public void fulfill(PathfinderPrerequisite prerequisite) {
                ExLog.i(getActivity(), TAG, "PathfinderPrerequisite");
//                RaceDialogFragment dialog = PrerequisiteRaceDialog.setPrerequisiteArguments(
//                        new RaceChoosePathFinderDialog(), getRace(), prerequisite);
//                dialog.show(getFragmentManager(), "userActionRequiredDialog");
            }

            @Override
            public void fulfill(StartmodePrerequisite prerequisite) {
                ExLog.i(getActivity(), TAG, "StartmodePrerequisite");
//                RaceDialogFragment dialog = PrerequisiteRaceDialog.setPrerequisiteArguments(
//                        new RaceChooseStartModeDialog(), getRace(), prerequisite);
//                dialog.show(getFragmentManager(), "userActionRequiredDialog");
            }

            @Override
            public void onFulfilled() {
                ExLog.i(getActivity(), TAG, "onFulfilled");
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
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_procedure_start_time, container, false);
        
        mTime = (TextView) view.findViewById(R.id.time_remaining);
        
        mDatePicker = (Button) view.findViewById(R.id.date);
        if (mDatePicker != null) {
            mDatePicker.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    getFragmentManager().beginTransaction().remove(mDatePickerFragment).commit();
                    mDatePickerFragment.show(getFragmentManager(), "datePicker");
                }
            });
        }
        
        mDatePickerFragment = new DatePickerFragment();
        setupDatePicker();
        
        mTimePicker = (TimePicker) view.findViewById(R.id.time);
        mTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
            
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                // TODO
            }
        });
        setupTimePicker();
                
        Button in5min = (Button) view.findViewById(R.id.button_5min);
        if (in5min != null) {
            in5min.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    addMinutes(5);
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

        Button confirm = (Button) view.findViewById(R.id.confirm);
        if (confirm != null) {
            confirm.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Save data
                }
            });
        }

        return view;
    }
    
    private void setupDatePicker() {
        if (mDatePickerFragment != null && mDatePicker != null) {
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
}
