package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.CourseFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.StartProcedureFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.WindFragment;

public class SetupPanelFragment extends BasePanelFragment {

    private RaceStateChangedListener mStateListener;
    private RaceProcedureChangedListener mProcedureListener;
    private IntentReceiver mReceiver;

    // Start Procedure Toggle
    private View mStartProcedure;
    private View mStartProcedureLock;
    private TextView mStartProcedureValue;

    // Start Procedure More Toggle
    private View mStartProcedureMore;
    private View mStartProcedureMoreLock;
    private FrameLayout mExtraLayout;

    // Course Toggle
    private View mCourse;
    private View mCourseLock;
    private TextView mCourseValue;

    // Wind Toggle
    private View mWind;
    private View mWindLock;
    private TextView mWindValue;

    public SetupPanelFragment() {
        mReceiver = new IntentReceiver();
    }

    public static SetupPanelFragment newInstance(Bundle args) {
        SetupPanelFragment fragment = new SetupPanelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_panel_setup, container, false);

        mStateListener = new RaceStateChangedListener();
        mProcedureListener = new RaceProcedureChangedListener();

        mStartProcedure = ViewHelper.get(layout, R.id.start_procedure);
        if (mStartProcedure != null) {
            mStartProcedure.setOnClickListener(new StartProcedureClick());
        }
        mStartProcedureLock = ViewHelper.get(layout, R.id.start_procedure_lock);
        mStartProcedureValue = ViewHelper.get(layout, R.id.start_procedure_value);

        mStartProcedureMore = ViewHelper.get(layout, R.id.start_procedure_more);
        if (mStartProcedureMore != null) {
            mStartProcedureMore.setOnClickListener(new StartProcedureMoreClick());
        }
        mStartProcedureMoreLock = ViewHelper.get(layout, R.id.start_procedure_more_lock);

        mCourse = ViewHelper.get(layout, R.id.course);
        if (mCourse != null) {
            mCourse.setOnClickListener(new CourseClick());
        }
        mCourseLock = ViewHelper.get(layout, R.id.course_lock);
        mCourseValue = ViewHelper.get(layout, R.id.course_value);

        mWind = ViewHelper.get(layout, R.id.wind);
        if (mWind != null) {
            mWind.setOnClickListener(new WindClick());
        }
        mWindLock = ViewHelper.get(layout, R.id.wind_lock);
        mWindValue = ViewHelper.get(layout, R.id.wind_value);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mExtraLayout = (FrameLayout) getActivity().findViewById(R.id.race_panel_extra);
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

        sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        getRaceState().removeChangedListener(mStateListener);
        getRaceState().getRacingProcedure().removeChangedListener(mProcedureListener);

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void refreshPanel() {
        if (mCourseValue != null) {
            mCourseValue.setText(null);
            mCourseValue.setCompoundDrawables(null, null, null, null);
            String courseName = getCourseName();
            mCourseValue.setText(courseName);
        }

        if (mStartProcedureValue != null && getRaceState().getTypedRacingProcedure() != null) {
            mStartProcedureValue.setText(getRaceState().getTypedRacingProcedure().getType().toString());

            if (mStartProcedureMore != null) {
                if (getRaceState().getRacingProcedure() instanceof RRS26RacingProcedure || getRaceState()
                    .getRacingProcedure() instanceof GateStartRacingProcedure) {
                    mStartProcedureMore.setVisibility(View.VISIBLE);
                } else {
                    mStartProcedureMore.setVisibility(View.GONE);
                }
            }
        }

        Wind wind = getRaceState().getWindFix();
        if (mWindValue != null && wind != null) {
            String sensorData = getString(R.string.wind_panel, wind.getFrom().getDegrees(), wind.getKnots());
            mWindValue.setText(sensorData);
        }
    }

    private void checkStatus() {
        switch (getRace().getStatus()) {
            case UNSCHEDULED:
                changeVisibility(mStartProcedureLock, View.GONE);
                changeVisibility(mStartProcedureMoreLock, View.GONE);
                changeVisibility(mCourseLock, View.GONE);
                changeVisibility(mWindLock, View.GONE);
                break;

            case PRESCHEDULED:
                changeVisibility(mStartProcedureLock, View.GONE);
                changeVisibility(mStartProcedureMoreLock, View.GONE);
                changeVisibility(mCourseLock, View.GONE);
                changeVisibility(mWindLock, View.GONE);
                break;

            case SCHEDULED:
                changeVisibility(mStartProcedureLock, View.GONE);
                changeVisibility(mStartProcedureMoreLock, View.GONE);
                changeVisibility(mCourseLock, View.GONE);
                changeVisibility(mWindLock, View.GONE);
                break;

            case STARTPHASE:
                changeVisibility(mStartProcedureLock, View.VISIBLE);
                changeVisibility(mStartProcedureMoreLock, View.GONE);
                changeVisibility(mCourseLock, View.GONE);
                changeVisibility(mWindLock, View.GONE);
                break;

            case RUNNING:
                changeVisibility(mStartProcedureLock, View.VISIBLE);
                changeVisibility(mStartProcedureMoreLock, View.GONE);
                changeVisibility(mCourseLock, View.GONE);
                changeVisibility(mWindLock, View.GONE);
                uncheckMarker(mStartProcedure);
                uncheckMarker(mStartProcedureMore);
                break;

            case FINISHING:
                changeVisibility(mStartProcedureLock, View.VISIBLE);
                changeVisibility(mStartProcedureMoreLock, View.GONE);
                changeVisibility(mCourseLock, View.VISIBLE);
                changeVisibility(mWindLock, View.GONE);
                uncheckMarker(mStartProcedure);
                uncheckMarker(mStartProcedureMore);
                uncheckMarker(mCourseLock);
                break;

            case FINISHED:
                changeVisibility(mStartProcedureLock, View.GONE);
                changeVisibility(mStartProcedureMoreLock, View.GONE);
                changeVisibility(mCourseLock, View.GONE);
                changeVisibility(mWindLock, View.GONE);
                uncheckMarker(mStartProcedure);
                uncheckMarker(mStartProcedureMore);
                uncheckMarker(mCourse);
                uncheckMarker(mWind);
                break;

            default:
                changeVisibility(mStartProcedureLock, View.VISIBLE);
                changeVisibility(mCourseLock, View.VISIBLE);
                changeVisibility(mWindLock, View.VISIBLE);
                uncheckMarker(mStartProcedure);
                uncheckMarker(mStartProcedureMore);
                uncheckMarker(mCourse);
                uncheckMarker(mWind);
                break;
        }
    }

    private void uncheckMarker(View view) {
        if (view != null) {
            if (!view.equals(mStartProcedure)) {
                resetFragment(mStartProcedureLock, getFrameId(getActivity(), R.id.race_edit, R.id.race_content), StartProcedureFragment.class);
                setMarkerLevel(mStartProcedure, R.id.start_procedure_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mStartProcedureMore)) {
                if (AppUtils.with(getActivity()).isTablet()) {
                    Fragment fragment = getFragmentManager().findFragmentById(R.id.race_panel_extra);
                    if (mExtraLayout != null && fragment != null) {
                        mExtraLayout.setVisibility(View.GONE);
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.remove(fragment);
                        transaction.commit();
                    }
                }
                setMarkerLevel(mStartProcedureMore, R.id.start_procedure_more_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mCourse)) {
                resetFragment(mCourseLock, getFrameId(getActivity(), R.id.race_edit, R.id.race_content), CourseFragment.class);
                setMarkerLevel(mCourse, R.id.course_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mWind)) {
                resetFragment(mWindLock, getFrameId(getActivity(), R.id.race_edit, R.id.race_content), WindFragment.class);
                setMarkerLevel(mWind, R.id.wind_marker, LEVEL_NORMAL);
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

            state.getRacingProcedure().addChangedListener(mProcedureListener);

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

    private class StartProcedureClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = StartProcedureClick.class.getName();
        private final View container = mStartProcedure;
        private final int markerId = R.id.start_procedure_marker;

        @Override
        public void onClick(View v) {
            if (mStartProcedureLock != null) {
                if (mStartProcedureLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(StartProcedureFragment.newInstance(1));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
                    break;
            }
            disableToggle(container, markerId);
        }
    }

    private class StartProcedureMoreClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = StartProcedureMoreClick.class.getName();
        private final View container = mStartProcedureMore;
        private final int markerId = R.id.start_procedure_more_marker;

        @Override
        public void onClick(View v) {
            if (mStartProcedureMoreLock != null) {
                if (mStartProcedureMoreLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (mExtraLayout != null) {
                switch (toggleMarker(container, markerId)) {
                    case LEVEL_NORMAL:
                        mExtraLayout.setVisibility(View.GONE);
                        transaction.remove(getFragmentManager().findFragmentById(R.id.race_panel_extra));
                        break;

                    case LEVEL_TOGGLED:
                        int multiplier = 1;
                        if (getRaceState().getRacingProcedure() instanceof GateStartRacingProcedure) {
                            multiplier = 2;
                        }
                        int height = container.getHeight();
                        int width = container.getWidth() * multiplier;
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                        params.setMargins(container.getLeft() + container.getWidth(), container.getTop(), 0, 0);
                        mExtraLayout.setLayoutParams(params);
                        mExtraLayout.setVisibility(View.VISIBLE);
                        transaction.replace(R.id.race_panel_extra, MorePanelFragment.newInstance(getArguments()));
                        break;

                    default:
                        ExLog.i(getActivity(), TAG, "Unknown return value");
                        break;
                }
            }
            transaction.commit();
            disableToggle(container, markerId);
        }
    }

    private class CourseClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = CourseClick.class.getName();
        private final View container = mCourse;
        private final int markerId = R.id.course_marker;

        @Override
        public void onClick(View v) {
            if (mCourseLock != null) {
                if (mCourseLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_COURSE);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(CourseFragment.newInstance(1, getRace()));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            disableToggle(container, markerId);
        }
    }

    private class WindClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = WindClick.class.getName();
        private final View container = mWind;
        private final int markerId = R.id.wind_marker;

        @Override
        public void onClick(View v) {
            if (mWindLock != null) {
                if (mWindLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_WIND);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(WindFragment.newInstance(1));
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
                    if (AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE.equals(data)) {
                        uncheckMarker(mStartProcedure);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE.equals(data)) {
                        uncheckMarker(mStartProcedureMore);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_COURSE.equals(data)) {
                        uncheckMarker(mCourse);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_WIND.equals(data)) {
                        uncheckMarker(mWind);
                    } else {
                        uncheckMarker(new View(context));
                    }
                }
            }
        }
    }
}
