package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.Calendar;
import java.util.Date;

import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.AbortModeSelectionDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;

public class SetStartTimeRaceFragment extends RaceFragment {

    public static SetStartTimeRaceFragment create(ManagedRace race) {
        SetStartTimeRaceFragment fragment = new SetStartTimeRaceFragment();
        fragment.setArguments(createArguments(race));
        return fragment;
    }

    protected boolean isReset;
    private StartProcedureType selectedStartProcedureType;

    protected Spinner spinnerStartProcedure;
    protected TimePicker pickerTime;
    protected Button btSetDate;
    protected Button btSetTime;
    protected Button btPostpone;
    protected TextView textInfoText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_reset_time, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isReset = getArguments().getBoolean(AppConstants.RESET_TIME_FRAGMENT_IS_RESET);

        spinnerStartProcedure = (Spinner) getView().findViewById(R.id.race_reset_time_spinner_procedure);
        setupStartProcedureSpinner();
        pickerTime = (TimePicker) getView().findViewById(R.id.race_reset_time_picker_time);
        setupTimePicker();
        btSetDate = (Button) getView().findViewById(R.id.race_reset_time_btn_date);
        btSetTime = (Button) getView().findViewById(R.id.race_reset_time_btn_time);
        btPostpone = (Button) getView().findViewById(R.id.race_reset_time_btn_postpone);

        btSetDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Not implemented yet.", Toast.LENGTH_SHORT).show();
            }
        });

        btSetTime.setText(isReset ? R.string.reset_time : R.string.set_time);
        btSetTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setStartTime();
            }
        });

        btPostpone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ExLog.i(ExLog.RACE_SET_TIME_BUTTON_AP, getRace().getId().toString(), getActivity());
                showAPModeDialog();
            }
        });

        textInfoText = (TextView) getView().findViewById(R.id.race_reset_time_text_infotext);
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
        ArrayAdapter<StartProcedureType> adapter = new ArrayAdapter<StartProcedureType>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, StartProcedureType.values());
        spinnerStartProcedure.setAdapter(adapter);
        spinnerStartProcedure.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedStartProcedureType = (StartProcedureType) adapterView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        StartProcedureType type = StartProcedureType.ESS;
        boolean overrideStartProcedureType = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(
                "overrideDefaultStartProcedureType", false);
        if (overrideStartProcedureType) {
            type = StartProcedureType.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
                    "defaultStartProcedureType", StartProcedureType.ESS.name()));
        } else {
            type = getRace().getState().getStartProcedureType();
        }
        spinnerStartProcedure.setSelection(adapter.getPosition(type));
    }

    @Override
    public void onStart() {
        super.onStart();
        ExLog.i(SetStartTimeRaceFragment.class.getName(),
                String.format("Fragment %s is now shown", SetStartTimeRaceFragment.class.getName()));
    }

    @Override
    public void notifyTick() {
        super.notifyTick();

        textInfoText.setText(String.format("Start %s", getTimeStringToStart()));
    }

    private Date getStartTime() {
        Calendar newStartTimeCal = Calendar.getInstance();
        // newStartTimeCal.set(Calendar.YEAR, 0);
        // newStartTimeCal.set(Calendar.MONTH, 0);
        // newStartTimeCal.set(Calendar.DAY_OF_MONTH, 0);
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
            timeText += String.format("%d hours%s, ", diff[1], diff[1] > 1 ? "s" : "");
        }
        if (diff[2] > 0) {
            timeText += String.format("%d minute%s, ", diff[2], diff[2] > 1 ? "s" : "");
        }
        timeText += String.format("%d second%s", diff[3], diff[3] > 1 ? "s" : "");

        return isInPast ? String.format("%s %s", timeText, "ago") : String.format("%s %s", "in", timeText);
    }

    protected void setStartTime() {
        if (isReset) {
            ExLog.i(ExLog.RACE_RESET_TIME, getRace().getId().toString(), getActivity());
        } else {
            ExLog.i(ExLog.RACE_SET_TIME, getRace().getId().toString(), getActivity());
        }
        setStartTime(getStartTime());

        // TODO: start course designer activity!
    }

    private void setStartTime(Date newStartTime) {
        getRace().getState().setStartTime(new MillisecondsTimePoint(newStartTime), selectedStartProcedureType);
    }

    protected void showAPModeDialog() {
        FragmentManager fragmentManager = getFragmentManager();

        RaceDialogFragment fragment = new AbortModeSelectionDialog();

        Bundle args = getRecentArguments();
        args.putString(AppConstants.FLAG_KEY, Flags.AP.name());
        fragment.setArguments(args);

        fragment.show(fragmentManager, "dialogAPMode");
    }

}
