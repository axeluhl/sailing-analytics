package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import com.sap.sailing.android.shared.util.ActivityHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class GateStartPathFinderFragment extends BaseFragment {

    private final static String NAT = "nationality";
    private final static String NUM = "number";

    private EditText mNat;
    private EditText mNum;
    private View mButton;

    public static GateStartPathFinderFragment newInstance() {
        return newInstance(START_MODE_PRESETUP, null, null);
    }

    public static GateStartPathFinderFragment newInstance(@START_MODE_VALUES int startMode) {
        return newInstance(startMode, null, null);
    }

    public static GateStartPathFinderFragment newInstance(String nat, String num) {
        return newInstance(START_MODE_PRESETUP, nat, num);
    }

    public static GateStartPathFinderFragment newInstance(@START_MODE_VALUES int startMode, String nat, String num) {
        GateStartPathFinderFragment fragment = new GateStartPathFinderFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        args.putString(NAT, nat);
        args.putString(NUM, num);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View layout = LayoutInflater.from(getActivity()).inflate(R.layout.race_schedule_procedure_pathfinder,
                container, false);

        mNat = ViewHelper.get(layout, R.id.pathfinder_nat);
        mNum = ViewHelper.get(layout, R.id.pathfinder_num);
        mButton = ViewHelper.get(layout, R.id.set_path_finder);

        if (getArguments() != null) {
            if (mNat != null) {
                mNat.setText(getArguments().getString(NAT));
                mNat.setFilters(new InputFilter[] { new InputFilter.AllCaps() });
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
                mNum.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
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

        HeaderLayout header = ViewHelper.get(layout, R.id.header);
        if (header != null) {
            header.setHeaderOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goHome();
                }
            });
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

    @Override
    public void onPause() {
        super.onPause();

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final GateStartRacingProcedure procedure = (GateStartRacingProcedure) getRaceState().getRacingProcedure();
        if (procedure != null) {
            if (mNat != null && TextUtils.isEmpty(mNat.getText())) {
                mNat.setText(extractPosition(0, procedure.getPathfinder()));
            }
            if (mNum != null && TextUtils.isEmpty(mNum.getText())) {
                mNum.setText(extractPosition(1, procedure.getPathfinder()));
            }
        }

        if (mNat != null) {
            mNat.requestFocus();
        }

        if (mButton != null) {
            mButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ActivityHelper.with(getActivity()).hideKeyboard();
                    String nation = "";
                    if (mNat != null) {
                        nation = mNat.getText().toString();
                    }
                    String number = "";
                    if (mNum != null) {
                        number = mNum.getText().toString();
                    }
                    if (procedure != null) {
                        procedure.setPathfinder(MillisecondsTimePoint.now(), String.format("%s%s", nation, number));
                    }
                    goHome();
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
