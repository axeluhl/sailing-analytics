package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.GateStartPathFinderFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.LineStartModeFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.GateStartTimingFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;

public class MorePanelFragment extends BasePanelFragment {

    private RaceStateChangedListener mStateListener;
    private RaceProcedureChangedListener mProcedureListener;
    private IntentReceiver mReceiver;

    // Start Mode Toggle
    private View mStartMode;
    private View mStartModeLock;
    private ImageView mStartModeFlag;

    // Gate Start PathFinder
    private View mGatePathfinder;
    private View mGatePathfinderLock;
    private TextView mGatePathfinderValue;

    // Gate Start Timing
    private View mGateTiming;
    private View mGateTimingLock;
    private TextView mGateTimingValue;

    public MorePanelFragment() {
        mReceiver = new IntentReceiver();
    }

    public static MorePanelFragment newInstance(Bundle args) {
        MorePanelFragment fragment = new MorePanelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_panel_more, container, false);

        mStateListener = new RaceStateChangedListener();
        mProcedureListener = new RaceProcedureChangedListener();

        mStartMode = ViewHelper.get(layout, R.id.start_mode);
        if (mStartMode != null) {
            mStartMode.setOnClickListener(new StartModeClick());
        }
        mStartModeLock = ViewHelper.get(layout, R.id.start_mode_lock);
        mStartModeFlag = ViewHelper.get(layout, R.id.start_mode_flag);

        mGatePathfinder = ViewHelper.get(layout, R.id.gate_pathfinder);
        if (mGatePathfinder != null) {
            mGatePathfinder.setOnClickListener(new PathfinderClick());
        }
        mGatePathfinderLock = ViewHelper.get(layout, R.id.gate_pathfinder_lock);
        mGatePathfinderValue = ViewHelper.get(layout, R.id.gate_pathfinder_value);

        mGateTiming = ViewHelper.get(layout, R.id.gate_timing);
        if (mGateTiming != null) {
            mGateTiming.setOnClickListener(new TimingClick());
        }
        mGateTimingLock = ViewHelper.get(layout, R.id.gate_timing_lock);
        mGateTimingValue = ViewHelper.get(layout, R.id.gate_timing_value);

        View view = ViewHelper.get(layout, R.id.top_line);
        if (view != null) {
            if (!AppUtils.with(getActivity()).is10inch()) {
                view.setVisibility(View.GONE);
            }
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshPanel();
        checkStatus();

        getRaceState().addChangedListener(mStateListener);
        getRaceState().getRacingProcedure().addChangedListener(mProcedureListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_TOGGLE);
        filter.addAction(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);

//        sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        getRaceState().removeChangedListener(mStateListener);
        getRaceState().getRacingProcedure().removeChangedListener(mProcedureListener);

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void refreshPanel() {
        RacingProcedure procedure = getRaceState().getRacingProcedure();
        if (procedure instanceof RRS26RacingProcedure) {
            RRS26RacingProcedure typedProcedure = getRaceState().getTypedRacingProcedure();
            if (mStartMode != null && mStartModeFlag != null) {
                mStartMode.setVisibility(View.VISIBLE);
                mStartModeFlag.setImageDrawable(FlagsResources
                    .getFlagDrawable(getActivity(), typedProcedure.getStartModeFlag().name(), getResources().getInteger(R.integer.flag_size)));
            }
        }

        if (procedure instanceof GateStartRacingProcedure) {
            GateStartRacingProcedure typedProcedure = getRaceState().getTypedRacingProcedure();
            if (mGatePathfinder != null && mGatePathfinderValue != null) {
                mGatePathfinder.setVisibility(View.VISIBLE);
                mGatePathfinderValue.setText(typedProcedure.getPathfinder());
            }

            if (mGateTiming != null && mGateTimingValue != null) {
                mGateTiming.setVisibility(View.VISIBLE);
                mGateTimingValue.setText(RaceHelper.getGateTiming(getActivity(), typedProcedure));
            }
        }
    }

    private void checkStatus() {
        switch (getRace().getStatus()) {
            case UNSCHEDULED:
                changeVisibility(mStartModeLock, View.GONE);
                changeVisibility(mGatePathfinderLock, View.GONE);
                changeVisibility(mGateTimingLock, View.GONE);
                break;

            case PRESCHEDULED:
                changeVisibility(mStartModeLock, View.GONE);
                changeVisibility(mGatePathfinderLock, View.GONE);
                changeVisibility(mGateTimingLock, View.GONE);
                break;

            case SCHEDULED:
                changeVisibility(mStartModeLock, View.GONE);
                changeVisibility(mGatePathfinderLock, View.GONE);
                changeVisibility(mGateTimingLock, View.GONE);
                break;

            case STARTPHASE:
                changeVisibility(mStartModeLock, View.VISIBLE);
                changeVisibility(mGatePathfinderLock, View.VISIBLE);
                changeVisibility(mGateTimingLock, View.VISIBLE);
                break;

            case RUNNING:
                changeVisibility(mStartModeLock, View.VISIBLE);
                changeVisibility(mGatePathfinderLock, View.VISIBLE);
                changeVisibility(mGateTimingLock, View.VISIBLE);
                uncheckMarker(mStartMode);
                break;

            case FINISHING:
                changeVisibility(mStartModeLock, View.VISIBLE);
                changeVisibility(mGatePathfinderLock, View.VISIBLE);
                changeVisibility(mGateTimingLock, View.VISIBLE);
                uncheckMarker(mStartMode);
                break;

            case FINISHED:
                changeVisibility(mStartModeLock, View.GONE);
                changeVisibility(mGatePathfinderLock, View.GONE);
                changeVisibility(mGateTimingLock, View.GONE);
                uncheckMarker(mStartMode);
                break;

            default:
                changeVisibility(mStartModeLock, View.VISIBLE);
                changeVisibility(mGatePathfinderLock, View.VISIBLE);
                changeVisibility(mGateTimingLock, View.VISIBLE);
                uncheckMarker(mStartMode);
                break;
        }
    }

    private void uncheckMarker(View view) {
        if (isAdded() && view != null) {
            if (!AppUtils.with(getActivity()).is10inch()) {
                if (!view.equals(mStartMode)) {
                    setMarkerLevel(mStartMode, R.id.start_mode_marker, LEVEL_NORMAL);
                }
                if (!view.equals(mGatePathfinder)) {
                    setMarkerLevel(mGatePathfinder, R.id.gate_pathfinder_marker, LEVEL_NORMAL);
                }
                if (!view.equals(mGateTiming)) {
                    setMarkerLevel(mGateTiming, R.id.gate_timing_marker, LEVEL_NORMAL);
                }
            }
        }
    }

    private class RaceStateChangedListener extends BaseRaceStateChangedListener {

        private View mView;

        public RaceStateChangedListener() {
            mView = new View(getActivity());
        }

        @Override
        public void onRacingProcedureChanged(ReadonlyRaceState state) {
            super.onRacingProcedureChanged(state);

            refreshPanel();
            uncheckMarker(mView);
        }

        @Override
        public void onCourseDesignChanged(ReadonlyRaceState state) {
            super.onCourseDesignChanged(state);

            refreshPanel();
            uncheckMarker(mView);
        }

        @Override
        public void onWindFixChanged(ReadonlyRaceState state) {
            super.onWindFixChanged(state);

            refreshPanel();
            uncheckMarker(mView);
        }

        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            super.onStatusChanged(state);

            checkStatus();
            uncheckMarker(mView);
        }
    }

    private class RaceProcedureChangedListener extends BaseRacingProcedureChangedListener {

        private View mView;

        public RaceProcedureChangedListener() {
            mView = new View(getActivity());
        }

        @Override
        public void onActiveFlagsChanged(ReadonlyRacingProcedure racingProcedure) {
            super.onActiveFlagsChanged(racingProcedure);

            refreshPanel();
            uncheckMarker(mView);
        }
    }

    private class StartModeClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = StartModeClick.class.getName();
        private final View container = mStartMode;
        private final int markerId = R.id.start_mode_marker;

        public void onClick(View v) {
            if (mStartModeLock != null) {
                if (mStartModeLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
                    showChangeDialog(this);
                } else {
                    toggleFragment();
                }
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            toggleFragment();
        }

        private void toggleFragment() {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_MODE);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(LineStartModeFragment.newInstance(LineStartModeFragment.START_MODE_PLANNED));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            disableToggle(container, markerId);
        }
    }

    private class PathfinderClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = PathfinderClick.class.getName();
        private final View container = mGatePathfinder;
        private final int markerId = R.id.gate_pathfinder_marker;

        @Override
        public void onClick(View v) {
            if (mGatePathfinderLock != null) {
                if (mGatePathfinderLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
                    showChangeDialog(this);
                } else {
                    toggleFragment();
                }
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            toggleFragment();
        }

        private void toggleFragment() {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_PATHFINDER);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(GateStartPathFinderFragment.newInstance(GateStartPathFinderFragment.START_MODE_PLANNED));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            disableToggle(container, markerId);
        }
    }

    private class TimingClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = TimingClick.class.getName();
        private final View container = mGateTiming;
        private final int markerId = R.id.gate_timing_marker;

        @Override
        public void onClick(View v) {
            if (mGateTimingLock != null) {
                if (mGateTimingLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
                    showChangeDialog(this);
                } else {
                    toggleFragment();
                }
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            toggleFragment();
        }

        private void toggleFragment() {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_TIMING);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(GateStartTimingFragment.newInstance(GateStartTimingFragment.START_MODE_PLANNED));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            disableToggle(container, markerId);
        }
    }

    private class IntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppConstants.INTENT_ACTION_CLEAR_TOGGLE.equals(action)) {
                uncheckMarker(new View(context));
            }

            if (AppConstants.INTENT_ACTION_TOGGLE.equals(action)) {
                if (intent.getExtras() != null) {
                    String data = intent.getExtras().getString(AppConstants.INTENT_ACTION_EXTRA);
                    if (AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_MODE.equals(data)) {
                        uncheckMarker(mStartMode);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_PATHFINDER.equals(data)) {
                        uncheckMarker(mGatePathfinder);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_TIMING.equals(data)) {
                        uncheckMarker(mGateTiming);
                    } else {
                        uncheckMarker(new View(context));
                    }
                }
            }
        }
    }
}
