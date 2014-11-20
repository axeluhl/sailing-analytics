package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl.GateLaunchTimePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl.PathfinderPrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.impl.StartmodePrerequisite;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.DatePickerFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.PrerequisiteRaceDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.RaceChooseGateLaunchTimesDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.RaceChoosePathFinderDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.RaceChooseStartModeDialog;
import com.sap.sailing.racecommittee.app.ui.utils.CourseDesignerChooser;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class SetStartTimeRaceFragment extends RaceFragment {

    private static final String KEY_STATE = "KEY";

    private static final String TAG = SetStartTimeRaceFragment.class.getName();

    public static SetStartTimeRaceFragment create(ManagedRace race) {
        SetStartTimeRaceFragment fragment = new SetStartTimeRaceFragment();
        fragment.setArguments(createArguments(race));
        return fragment;
    }

    protected boolean isReset;

    protected Spinner spinnerStartProcedure;
    protected TimePicker pickerTime;
    protected Button btSetDate;
    protected Button btSetTime;
    protected Button btPostpone;
    protected Button btSetCourse;
    protected TextView textInfoText;

    protected DatePickerFragment datePicker;
    

    private RacingProcedureType selectedStartProcedureType;
    private SetStartTimeState state;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_reset_time, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isReset = getArguments().getBoolean(AppConstants.RESET_TIME_FRAGMENT_IS_RESET);

        datePicker = new DatePickerFragment();
        setupDatePicker();

        spinnerStartProcedure = (Spinner) getView().findViewById(R.id.race_reset_time_spinner_procedure);
        setupStartProcedureSpinner();
        pickerTime = (TimePicker) getView().findViewById(R.id.race_reset_time_picker_time);
        setupTimePicker();
        btSetDate = (Button) getView().findViewById(R.id.race_reset_time_btn_date);
        btSetTime = (Button) getView().findViewById(R.id.race_reset_time_btn_time);
        btPostpone = (Button) getView().findViewById(R.id.race_reset_time_btn_postpone);
        btSetCourse = (Button) getView().findViewById(R.id.race_set_course);

        btSetDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker.show(getFragmentManager(), "datePicker");
            }
        });

        btSetTime.setText(isReset ? R.string.reset_time : R.string.set_time);
        btSetTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setStartTime();
            }
        });
        
        if (btSetCourse != null) {
            btSetCourse.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View paramView) {
                    showCourseDesignDialog();
                }
            });
        }

        if (btSetCourse != null) {
            btPostpone.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    ExLog.i(getActivity(), LogEvent.RACE_SET_TIME_BUTTON_AP, getRace().getId().toString());
                    showAPModeDialog();
                }
            });
        }

        textInfoText = (TextView) getView().findViewById(R.id.race_reset_time_text_infotext);
        
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_STATE)) {
            ExLog.i(getActivity(), TAG, "Restoring set start time state...");
            this.state = savedInstanceState.getParcelable(KEY_STATE);
            setStartTime(this.state);
        }
    }

    private void setupDatePicker() {
        datePicker.setOnDateSetListener(new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar startDate = Calendar.getInstance();
                startDate.set(year, month, day);
                Calendar today = Calendar.getInstance();

                if (startDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                        && startDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    btSetDate.setText(R.string.today);
                } else {
                    String dateString = DateFormat.getDateFormat(getActivity()).format(startDate.getTime());
                    btSetDate.setText(dateString);
                }
            }
        });
        final Calendar today = Calendar.getInstance();
        datePicker.setCurrentYear(today.get(Calendar.YEAR));
        datePicker.setCurrentMonth(today.get(Calendar.MONTH));
        datePicker.setCurrentDay(today.get(Calendar.DAY_OF_MONTH));
    }

    private void showCourseDesignDialog() {
        RaceDialogFragment fragment = CourseDesignerChooser.choose(preferences, getRace());
        fragment.setArguments(getRecentArguments());
        fragment.show(getFragmentManager(), "courseDesignDialogFragment");
    }

    private void setupTimePicker() {
        pickerTime.setIs24HourView(true);

        // In 10 minutes from now, but always a 5-minute-mark.
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 10);
        int hours = now.get(Calendar.HOUR_OF_DAY);
        int minutes = now.get(Calendar.MINUTE);
        minutes = (int) (Math.ceil((minutes / 5.0)) * 5.0);
        if (minutes >= 60) {
            hours++;
        }

        pickerTime.setCurrentHour(hours);
        pickerTime.setCurrentMinute(minutes);
    }

    private void setupStartProcedureSpinner() {
        ArrayAdapter<RacingProcedureType> adapter = new ArrayAdapter<RacingProcedureType>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, RacingProcedureType.validValues());
        spinnerStartProcedure.setAdapter(adapter);
        spinnerStartProcedure.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedStartProcedureType = (RacingProcedureType) adapterView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        RacingProcedureType configType = getRaceState().getConfiguration().getDefaultRacingProcedureType();
        spinnerStartProcedure.setSelection(adapter.getPosition(configType));
    }

    @Override
    public void onStart() {
        super.onStart();
        ExLog.i(getActivity(), SetStartTimeRaceFragment.class.getName(),
                String.format("Fragment %s is now shown", SetStartTimeRaceFragment.class.getName()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (state != null) {
            ExLog.i(getActivity(), TAG, "Storing start time state...");
            outState.putParcelable(KEY_STATE, state);
        }
    }

    @Override
    public void notifyTick() {
        super.notifyTick();

        textInfoText.setText(String.format("Start %s", getTimeStringToStart()));
    }

    private Date getStartTime() {
        Calendar newStartTimeCal = Calendar.getInstance();
        newStartTimeCal.set(Calendar.YEAR, datePicker.getCurrentYear());
        newStartTimeCal.set(Calendar.MONTH, datePicker.getCurrentMonth());
        newStartTimeCal.set(Calendar.DAY_OF_MONTH, datePicker.getCurrentDay());
        newStartTimeCal.set(Calendar.HOUR_OF_DAY, pickerTime.getCurrentHour());
        newStartTimeCal.set(Calendar.MINUTE, pickerTime.getCurrentMinute());
        newStartTimeCal.set(Calendar.SECOND, 0);
        newStartTimeCal.set(Calendar.MILLISECOND, 0);
        Date newStartTime = newStartTimeCal.getTime();
        return newStartTime;
    }

    private String getTimeStringToStart() {
        Date startTime = getStartTime();
        Date now = new Date();

        long differenceInSeconds = (startTime.getTime() - now.getTime()) / 1000;
        boolean isInPast = Math.signum(differenceInSeconds) < 0;
        if (isInPast) {
            differenceInSeconds = Math.abs(differenceInSeconds);
        }

        long diff[] = new long[] { 0, 0, 0, 0 };
        /* sec */diff[3] = (differenceInSeconds >= 60 ? differenceInSeconds % 60 : differenceInSeconds);
        /* min */diff[2] = (differenceInSeconds = (differenceInSeconds / 60)) >= 60 ? differenceInSeconds % 60
                : differenceInSeconds;
        /* hours */diff[1] = (differenceInSeconds = (differenceInSeconds / 60)) >= 24 ? differenceInSeconds % 24
                : differenceInSeconds;
        /* days */diff[0] = (differenceInSeconds = (differenceInSeconds / 24));

        String timeText = "";
        if (diff[0] > 0) {
            timeText += String.format("%d day%s, ", diff[0], diff[0] > 1 ? "s" : "");
        }
        if (diff[1] > 0) {
            timeText += String.format("%d hour%s, ", diff[1], diff[1] > 1 ? "s" : "");
        }
        if (diff[2] > 0) {
            timeText += String.format("%d minute%s, ", diff[2], diff[2] > 1 ? "s" : "");
        }
        timeText += String.format("%d second%s", diff[3], diff[3] > 1 ? "s" : "");

        return isInPast ? String.format("%s %s", timeText, "ago") : String.format("%s %s", "in", timeText);
    }

    protected void setStartTime() {
        if (isReset) {
            ExLog.i(getActivity(), LogEvent.RACE_RESET_TIME, getRace().getId().toString());
        } else {
            ExLog.i(getActivity(), LogEvent.RACE_SET_TIME, getRace().getId().toString());
        }
        setStartTime(getStartTime());
    }

    private void setStartTime(Date newStartTime) {
        final TimePoint now = MillisecondsTimePoint.now();

        getRaceState().setAdvancePass(now);
        getRaceState().setRacingProcedure(now, selectedStartProcedureType);
        
        this.state = new SetStartTimeState(now, new MillisecondsTimePoint(newStartTime));
        setStartTime(state);
    }

    private void setStartTime(SetStartTimeState state) {
        getRaceState().requestNewStartTime(state.now, state.startTime, resolver);
    }

    protected void showAPModeDialog() {
        RaceDialogFragment fragment = new AbortModeSelectionDialog();
        Bundle args = getRecentArguments();
        args.putString(AppConstants.FLAG_KEY, Flags.AP.name());
        fragment.setArguments(args);
        fragment.show(getFragmentManager(), "dialogAPMode");
    }

    RacingProcedurePrerequisite.Resolver resolver = new RacingProcedurePrerequisite.Resolver() {
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
            state = null;
        }
    };

    public static class SetStartTimeState implements Parcelable {

        private final TimePoint now;
        private final TimePoint startTime;
        
        public SetStartTimeState(TimePoint now, TimePoint startTime) {
            this.now = now;
            this.startTime = startTime;
        }

        private SetStartTimeState(Parcel in) {
            now = (TimePoint) in.readSerializable();
            startTime = (TimePoint) in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(now);
            dest.writeSerializable(startTime);
        }

        public static final Parcelable.Creator<SetStartTimeState> CREATOR = new Parcelable.Creator<SetStartTimeState>() {
            public SetStartTimeState createFromParcel(Parcel in) {
                return new SetStartTimeState(in);
            }

            public SetStartTimeState[] newArray(int size) {
                return new SetStartTimeState[size];
            }
        };
        
        @Override
        public int describeContents() {
            return 0;
        }
    }

}
