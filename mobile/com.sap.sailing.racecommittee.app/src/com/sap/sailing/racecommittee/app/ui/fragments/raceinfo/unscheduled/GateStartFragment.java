package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.ArrayList;

public class GateStartFragment {

    private static int ONE_MINUTE_MILLISECONDS = 60000;
    private static String NAT = "nationality";
    private static String NUM = "number";

    public static class Pathfinder extends ScheduleFragment {

        EditText mNat;
        EditText mNum;
        View mHeader;
        View mButton;

        public static Pathfinder newInstance() {
            return newInstance(null, null);
        }

        public static Pathfinder newInstance(String nat, String num) {
            Pathfinder fragment = new Pathfinder();
            Bundle args = new Bundle();
            args.putString(NAT, nat);
            args.putString(NUM, num);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.race_schedule_procedure_gate_start_pathfinder, container, false);

            mNat = (EditText) view.findViewById(R.id.pathfinder_nat);
            mNum = (EditText) view.findViewById(R.id.pathfinder_num);
            mHeader = view.findViewById(R.id.header_text);
            mButton = view.findViewById(R.id.set_path_finder);

            if (getArguments() != null) {
                if (mNat != null) {
                    mNat.setText(getArguments().getString(NAT));
                    mNat.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            // nothing
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            // nothing
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            view.setTag(R.id.pathfinder_nat, s.length() != 0);
                            enableSetButton(view, mButton);
                        }
                    });
                }

                if (mNum != null) {
                    mNum.setText(getArguments().getString(NUM));
                    mNum.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            // nothing
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            // nothing
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            view.setTag(R.id.pathfinder_num, s.length() != 0);
                            enableSetButton(view, mButton);
                        }
                    });
                }
            }

            if (mHeader != null) {
                mHeader.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openFragment(StartProcedureFragment.newInstance());
                    }
                });
            }

            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (mButton != null) {
                mButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String nation = "";
                        if (mNat != null) {
                            nation = mNat.getText().toString();
                        }
                        String number = "";
                        if (mNum != null) {
                            number = mNum.getText().toString();
                        }
                        GateStartRacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                        procedure.setPathfinder(MillisecondsTimePoint.now(), String.format("%s%s", nation, number));
                        openFragment(Timing.newInstance(nation, number));
                    }
                });
            }
        }

        private void enableSetButton(View view, View button) {
            if (view.getTag(R.id.pathfinder_nat) != null && view.getTag(R.id.pathfinder_num) != null) {
                button.setEnabled((Boolean) view.getTag(R.id.pathfinder_nat) && (Boolean) view.getTag(R.id.pathfinder_num));
            }
        }
    }

    public static class Timing extends ScheduleFragment {

        public static Timing newInstance(String nat, String num) {
            Timing fragment = new Timing();
            Bundle args = new Bundle();
            args.putString(NAT, nat);
            args.putString(NUM, num);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.race_schedule_procedure_gate_start_timing, container, false);

            View header = view.findViewById(R.id.header_text);
            if (header != null) {
                header.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        openFragment(Pathfinder.newInstance(getArguments().getString(NAT, null), getArguments().getString(NUM, null)));
                    }
                });
            }

            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            ArrayList<String> timeValues = getTimeValues();
            final NumberPicker time_launch = (NumberPicker) getActivity().findViewById(R.id.time_launch);
            if (time_launch != null) {
                time_launch.setMinValue(0);
                time_launch.setMaxValue(timeValues.size() - 1);
                time_launch.setDisplayedValues(timeValues.toArray(new String[timeValues.size()]));
                int value = (int)GateStartRacingProcedure.DefaultGateLaunchStopTime / ONE_MINUTE_MILLISECONDS;
                time_launch.setValue(value - 1);
            }

            final NumberPicker time_golf = (NumberPicker) getActivity().findViewById(R.id.time_golf);
            if (time_golf != null) {
                time_golf.setMinValue(0);
                time_golf.setMaxValue(timeValues.size() - 1);
                time_golf.setDisplayedValues(timeValues.toArray(new String[timeValues.size()]));
                int value = (int)GateStartRacingProcedure.DefaultGolfDownTime / ONE_MINUTE_MILLISECONDS;
                time_golf.setValue(value - 1);
            }

            View button = getActivity().findViewById(R.id.set_gate_time);
            if (button != null) {
                button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long launch = 0;
                        long golf = 0;

                        if (time_launch != null) {
                            launch = (time_launch.getValue() - 1) * ONE_MINUTE_MILLISECONDS;
                        }
                        if (time_golf != null) {
                            golf = (time_golf.getValue() - 1) * ONE_MINUTE_MILLISECONDS;
                        }

                        GateStartRacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                        procedure.setGateLineOpeningTimes(MillisecondsTimePoint.now(), launch, golf);
                        openMainScheduleFragment();
                    }
                });
            }
        }

        private ArrayList<String> getTimeValues() {
            ArrayList<String> timeValue = new ArrayList<>();
            for (int i = 1; i < 20; i++) {
                timeValue.add(String.format("%02d min", i));
            }
            return timeValue;
        }
    }
}
