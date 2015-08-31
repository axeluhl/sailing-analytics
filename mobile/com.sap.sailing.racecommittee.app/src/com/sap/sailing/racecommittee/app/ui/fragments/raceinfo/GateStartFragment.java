package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GateStartFragment {

    private final static int ONE_MINUTE_MILLISECONDS = 60000;
    private final static String START_MODE = "startMode";
    private final static String NAT = "nationality";
    private final static String NUM = "number";

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

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
            final View layout = LayoutInflater.from(getActivity()).inflate(R.layout.race_schedule_procedure_pathfinder, container, false);

            mNat = ViewHelper.get(layout, R.id.pathfinder_nat);
            mNum = ViewHelper.get(layout, R.id.pathfinder_num);
            mHeader = ViewHelper.get(layout, R.id.header_text);
            mButton = ViewHelper.get(layout, R.id.set_path_finder);

            if (getArguments() != null) {
                if (mNat != null) {
                    mNat.setText(getArguments().getString(NAT));
                    mNat.setFilters(new InputFilter[] { new InputFilter.AllCaps(), new InputFilter.LengthFilter(3) });
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
                    mNum.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
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
                        View header = ViewHelper.get(getView(), R.id.header);
                        if (header != null) {
                            header.setVisibility(View.GONE);
                        }
                    }
                }
            }

            final GateStartRacingProcedure procedure = getRaceState().getTypedRacingProcedure();
            if (mNat != null && TextUtils.isEmpty(mNat.getText())) {
                mNat.setText(extractPosition(0, procedure.getPathfinder()));
            }
            if (mNum != null && TextUtils.isEmpty(mNum.getText())) {
                mNum.setText(extractPosition(1, procedure.getPathfinder()));
            }

            if (mButton != null) {
                mButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        hideKeyboard(getActivity());
                        String nation = "";
                        if (mNat != null) {
                            nation = mNat.getText().toString();
                        }
                        String number = "";
                        if (mNum != null) {
                            number = mNum.getText().toString();
                        }
                        procedure.setPathfinder(MillisecondsTimePoint.now(), String.format("%s%s", nation, number));
                        if (getArguments() != null && getArguments().getInt(START_MODE, 0) == 0) {
                            replaceFragment(Timing.newInstance(nation, number));
                        } else {
                            replaceFragment(Timing.newInstance(1, nation, number), R.id.race_frame);
                        }
                    }
                });

                if (!TextUtils.isEmpty(mNat.getText()) && !TextUtils.isEmpty(mNum.getText())) {
                    mButton.setEnabled(true);
                }
            }
        }

        private String extractPosition(int pos, String string) {
            if (!TextUtils.isEmpty(string)) {
                String[] split = string.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                if (split.length == 2) {
                    return split[pos];
                }
            }
            return null;
        }

        private void enableSetButton(View view, View button) {
            if (view != null && view.getTag(R.id.pathfinder_nat) != null && view.getTag(R.id.pathfinder_num) != null) {
                button.setEnabled((Boolean) view.getTag(R.id.pathfinder_nat) && (Boolean) view.getTag(R.id.pathfinder_num));
            }
        }
    }

    public static class Timing extends BaseFragment {

        private final static String START_MODE = "startMode";

        private final int MIN_VALUE = 0;
        private final int MAX_VALUE = 60;

        private TextView mTotalTime;
        private NumberPicker mTimeLaunch;
        private NumberPicker mTimeGolf;
        private GateStartRacingProcedure mProcedure;

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
            View layout = LayoutInflater.from(getActivity()).inflate(R.layout.race_schedule_procedure_timing, container, false);

            View caption = ViewHelper.get(layout, R.id.header_text);
            if (caption != null) {
                caption.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        replaceFragment(Pathfinder.newInstance(getArguments().getString(NAT, null), getArguments().getString(NUM, null)));
                    }
                });
            }

            View additional = ViewHelper.get(layout, R.id.addition_golf_time);
            if (additional != null) {
                if (AppPreferences.on(getActivity()).getGateStartHasAdditionalGolfDownTime()) {
                    additional.setVisibility(View.VISIBLE);
                }
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

            mTotalTime = (TextView) getActivity().findViewById(R.id.total_time_text);
            mTimeLaunch = (NumberPicker) getActivity().findViewById(R.id.time_launch);
            mTimeGolf = (NumberPicker) getActivity().findViewById(R.id.time_golf);
            mProcedure = getRaceState().getTypedRacingProcedure();
            ViewHelper.disableSave(mTimeLaunch);
            ThemeHelper.setPickerColor(getActivity(), mTimeLaunch, ThemeHelper.getColor(getActivity(), R.attr.white), ThemeHelper
                .getColor(getActivity(), R.attr.sap_yellow_1));
            ViewHelper.disableSave(mTimeGolf);
            ThemeHelper.setPickerColor(getActivity(), mTimeGolf, ThemeHelper.getColor(getActivity(), R.attr.white), ThemeHelper
                .getColor(getActivity(), R.attr.sap_yellow_1));

            setTimeLaunchWidget();
            setTimeGolfWidget();
            setButton();
        }

        private void setTimeLaunchWidget() {
            if (mTimeLaunch != null) {
                mTimeLaunch.setMinValue(MIN_VALUE);
                mTimeLaunch.setMaxValue(MAX_VALUE);
                mTimeLaunch.setWrapSelectorWheel(false);
                int value = (int) (mProcedure.getGateLaunchStopTime() / ONE_MINUTE_MILLISECONDS);
                mTimeLaunch.setValue(value);
                mTimeLaunch.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        mTotalTime.setText("" + (newVal + mTimeGolf.getValue()));
                    }
                });
            }
        }

        private void setTimeGolfWidget() {
            if (mTimeGolf != null) {
                mTimeGolf.setMinValue(MIN_VALUE);
                mTimeGolf.setMaxValue(MAX_VALUE);
                mTimeGolf.setWrapSelectorWheel(false);
                int value = (int) (mProcedure.getGolfDownTime() / ONE_MINUTE_MILLISECONDS);
                mTimeGolf.setValue(value);
                mTimeGolf.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        int launch = 0;
                        if (mTimeLaunch != null) {
                            launch = mTimeLaunch.getValue();
                        }
                        mTotalTime.setText("" + (newVal + launch));
                    }
                });
                int launch = 0;
                if (mTimeLaunch != null) {
                    launch = mTimeLaunch.getValue();
                }
                mTotalTime.setText("" + (launch + mTimeGolf.getValue()));
            }
        }

        private void setButton() {
            View button = getActivity().findViewById(R.id.set_gate_time);
            if (button != null) {
                button.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        hideKeyboard(getActivity());
                        long launch = 0;
                        long golf = 0;

                        if (mTimeLaunch != null) {
                            launch = mTimeLaunch.getValue() * ONE_MINUTE_MILLISECONDS;
                        }
                        if (mTimeGolf != null) {
                            golf = mTimeGolf.getValue() * ONE_MINUTE_MILLISECONDS;

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
    }
}
