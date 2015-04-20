package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.ColorHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.ArrayList;

public class GateStartFragment {

    private final static int ONE_MINUTE_MILLISECONDS = 60000;
    private final static String START_MODE = "startMode";
    private final static String NAT = "nationality";
    private final static String NUM = "number";

    public static class Pathfinder extends BaseFragment {

        private EditText mNat;
        private EditText mNum;
        private View mHeader;
        private View mButton;

        public static Pathfinder newInstance() {
            return newInstance(0, null, null);
        }

        public static Pathfinder newInstance(int startMode) {
            return newInstance(startMode, null, null);
        }

        public static Pathfinder newInstance(String nat, String num) {
            return newInstance(0, nat, num);
        }

        public static Pathfinder newInstance(int startMode, String nat, String num) {
            Pathfinder fragment = new Pathfinder();
            Bundle args = new Bundle();
            args.putInt(START_MODE, startMode);
            args.putString(NAT, nat);
            args.putString(NUM, num);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View layout = inflater.inflate(R.layout.race_schedule_procedure_gate_start_pathfinder, container, false);

            mNat = ViewHolder.get(layout, R.id.pathfinder_nat);
            mNum = ViewHolder.get(layout, R.id.pathfinder_num);
            mHeader = ViewHolder.get(layout, R.id.header_text);
            mButton = ViewHolder.get(layout, R.id.set_path_finder);

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
                            layout.setTag(R.id.pathfinder_nat, s.length() != 0);
                            enableSetButton(layout, mButton);
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
                            layout.setTag(R.id.pathfinder_num, s.length() != 0);
                            enableSetButton(layout, mButton);
                        }
                    });
                }
            }

            if (mHeader != null) {
                mHeader.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        replaceFragment(StartProcedureFragment.newInstance(0));
                    }
                });
            }

            return layout;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (getArguments() != null) {
                if (getArguments().getInt(START_MODE, 0) != 0) {
                    if (getView() != null) {
                        View header = ViewHolder.get(getView(), R.id.header);
                        if (header != null) {
                            header.setVisibility(View.GONE);
                        }
                    }
                }
            }

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
                        if (getArguments() != null && getArguments().getInt(START_MODE, 0) == 0) {
                            replaceFragment(Timing.newInstance(nation, number));
                        } else {
                            replaceFragment(Timing.newInstance(1, nation, number), R.id.race_frame);
                        }
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

    public static class Timing extends BaseFragment {

        private final static String START_MODE = "startMode";

        public static Timing newInstance(String nat, String num) {
            return newInstance(0, nat, num);
        }

        public static Timing newInstance(int startMode, String nat, String num) {
            Timing fragment = new Timing();
            Bundle args = new Bundle();
            args.putInt(START_MODE, startMode);
            args.putString(NAT, nat);
            args.putString(NUM, num);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View layout = inflater.inflate(R.layout.race_schedule_procedure_gate_start_timing, container, false);

            View caption = ViewHolder.get(layout, R.id.header_text);
            if (caption != null) {
                caption.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        replaceFragment(Pathfinder.newInstance(getArguments().getString(NAT, null), getArguments().getString(NUM, null)));
                    }
                });
            }

            return layout;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (getArguments() != null) {
                if (getArguments().getInt(START_MODE, 0) != 0) {
                    if (getView() != null) {
                        View header = getView().findViewById(R.id.header);
                        if (header != null) {
                            header.setVisibility(View.GONE);
                        }
                    }
                }
            }
            final TextView totalTimeText = (TextView) getActivity().findViewById(R.id.total_time_text);
            ArrayList<String> timeValues = getTimeValues();
            final NumberPicker time_launch = (NumberPicker) getActivity().findViewById(R.id.time_launch);
            final NumberPicker time_golf = (NumberPicker) getActivity().findViewById(R.id.time_golf);
            ThemeHelper.setPickerTextColor(getActivity(), time_launch, ColorHelper.getThemedColor(getActivity(), R.attr.white));
            ThemeHelper.setPickerTextColor(getActivity(), time_golf, ColorHelper.getThemedColor(getActivity(), R.attr.white));
            if (time_launch != null) {
                time_launch.setMinValue(0);
                time_launch.setMaxValue(timeValues.size() - 1);
                int value = (int) GateStartRacingProcedure.DefaultGateLaunchStopTime / ONE_MINUTE_MILLISECONDS;
                time_launch.setValue(value - 1);
                time_launch.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        totalTimeText.setText("" + ((newVal) + (time_golf.getValue())) + " min");
                    }
                });
            }


            if (time_golf != null) {
                time_golf.setMinValue(0);
                time_golf.setMaxValue(timeValues.size() - 1);
                int value = (int) GateStartRacingProcedure.DefaultGolfDownTime / ONE_MINUTE_MILLISECONDS;
                time_golf.setValue(value - 1);
                time_golf.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        totalTimeText.setText("" + ((newVal)  + (time_launch.getValue() )) + " min");
                    }
                });
                totalTimeText.setText("" + ((time_launch.getValue() )  + (time_golf.getValue())) + " min");
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
                        if (getArguments() != null && getArguments().getInt(START_MODE, 0) == 0) {
                            openMainScheduleFragment();
                        } else {
                            sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                        }
                    }
                });
            }
        }

        private ArrayList<String> getTimeValues() {
            ArrayList<String> timeValue = new ArrayList<>();
            for (int i = 1; i < 20; i++) {
                timeValue.add(String.format("%02d", i));
            }
            return timeValue;
        }
    }
}
