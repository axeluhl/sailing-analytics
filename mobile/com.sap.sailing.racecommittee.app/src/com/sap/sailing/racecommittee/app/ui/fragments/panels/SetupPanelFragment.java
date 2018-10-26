package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import java.text.DecimalFormat;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ess.ESSRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.CourseFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.GateStartPathFinderFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.GateStartTimingFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RaceFactorFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.StartModeFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.StartProcedureFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.WindFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.ui.views.PanelButton;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SetupPanelFragment extends BasePanelFragment {

    private final static String ARGS_PAGE = "page";

    private RaceStateChangedListener mStateListener;
    private RaceProcedureChangedListener mProcedureListener;
    private IntentReceiver mReceiver;

    private PanelButton mButtonProcedure;
    private PanelButton mButtonMode;
    private PanelButton mButtonPathfinder;
    private PanelButton mButtonTiming;
    private PanelButton mButtonRaceGroup;
    private PanelButton mButtonFactor;
    private PanelButton mButtonCourse;
    private PanelButton mButtonWind;

    private DecimalFormat mFactorFormat;

    public SetupPanelFragment() {
        mReceiver = new IntentReceiver();
    }

    public static SetupPanelFragment newInstance(Bundle args, int page) {
        SetupPanelFragment fragment = new SetupPanelFragment();
        args.putInt(ARGS_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout;
        switch (getArguments().getInt(ARGS_PAGE, 0)) {
        case 1:
            layout = inflater.inflate(R.layout.race_panel_setup_hor_2, container, false);
            break;

        default:
            layout = inflater.inflate(R.layout.race_panel_setup, container, false);
        }

        mStateListener = new RaceStateChangedListener();
        mProcedureListener = new RaceProcedureChangedListener();

        mButtonProcedure = ViewHelper.get(layout, R.id.button_procedure);
        if (mButtonProcedure != null) {
            mButtonProcedure.setListener(new StartProcedureListener());
        }

        mButtonMode = ViewHelper.get(layout, R.id.button_mode);
        if (mButtonMode != null) {
            mButtonMode.setListener(new ButtonModeListener());
        }

        mButtonPathfinder = ViewHelper.get(layout, R.id.button_pathfinder);
        if (mButtonPathfinder != null) {
            mButtonPathfinder.setListener(new ButtonPathfinderListener());
        }

        mButtonTiming = ViewHelper.get(layout, R.id.button_timing);
        if (mButtonTiming != null) {
            mButtonTiming.setListener(new ButtonTimingListener());
        }

        mButtonRaceGroup = ViewHelper.get(layout, R.id.button_race_group);
        if (mButtonRaceGroup != null) {
            mButtonRaceGroup.setListener(new ButtonRaceGroupListener());
        }

        mButtonFactor = ViewHelper.get(layout, R.id.button_factor);
        if (mButtonFactor != null) {
            mButtonFactor.setListener(new ButtonFactorListener());
        }

        mButtonCourse = ViewHelper.get(layout, R.id.button_course);
        if (mButtonCourse != null) {
            mButtonCourse.setListener(new ButtonCourseListener());
        }

        mButtonWind = ViewHelper.get(layout, R.id.button_wind);
        if (mButtonWind != null) {
            mButtonWind.setListener(new ButtonWindListener());
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
        filter.addAction(AppConstants.INTENT_ACTION_UPDATE_SCREEN);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);

        sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFactorFormat = new DecimalFormat(getActivity().getString(R.string.race_factor_format));
    }

    @Override
    public void onPause() {
        super.onPause();

        getRaceState().removeChangedListener(mStateListener);
        getRaceState().getRacingProcedure().removeChangedListener(mProcedureListener);

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void refreshPanel() {
        if (getRaceState().getTypedRacingProcedure() != null) {
            if (mButtonProcedure != null) {
                mButtonProcedure.setPanelText(getRaceState().getTypedRacingProcedure().getType().toString());
            }

            if (mButtonMode != null) {
                mButtonMode.setVisibility(View.GONE);
            }
            if (mButtonPathfinder != null) {
                mButtonPathfinder.setVisibility(View.GONE);
            }
            if (mButtonTiming != null) {
                mButtonTiming.setVisibility(View.GONE);
            }
            if (mButtonRaceGroup != null) {
                mButtonRaceGroup.setVisibility(View.GONE);
            }

            if (getRaceState().getRacingProcedure() instanceof ConfigurableStartModeFlagRacingProcedure) {
                if (mButtonMode != null) {
                    ConfigurableStartModeFlagRacingProcedure typedProcedure = getRaceState().getTypedRacingProcedure();
                    mButtonMode.setPanelImage(FlagsResources.getFlagDrawable(getActivity(),
                            typedProcedure.getStartModeFlag().name(), getResources().getInteger(R.integer.flag_size)));
                    mButtonMode.setVisibility(View.VISIBLE);
                }
            }
            if (getRaceState().getRacingProcedure() instanceof GateStartRacingProcedure) {
                GateStartRacingProcedure typedProcedure = getRaceState().getTypedRacingProcedure();
                if (mButtonPathfinder != null) {
                    mButtonPathfinder.setPanelText(typedProcedure.getPathfinder());
                    mButtonPathfinder.setVisibility(View.VISIBLE);
                }
                if (mButtonTiming != null) {
                    mButtonTiming.setPanelText(
                            RaceHelper.getGateTiming(getActivity(), typedProcedure, getRace().getRaceGroup()));
                    mButtonTiming.setVisibility(View.VISIBLE);
                }
            }
            if (getRaceState().getRacingProcedure() instanceof ESSRacingProcedure) {
                if (mButtonRaceGroup != null) {
                    mButtonRaceGroup.setVisibility(View.VISIBLE);
                    mButtonRaceGroup.setPanelSwitch(getRaceState().isAdditionalScoringInformationEnabled(
                            AdditionalScoringInformationType.MAX_POINTS_DECREASE_MAX_SCORE));
                }
            }
        }

        if (mButtonFactor != null) {
            mButtonFactor.setVisibility(preferences.isRaceFactorChangeAllow() ? View.VISIBLE : View.GONE);
            if (getRace().getExplicitFactor() != null) {
                mButtonFactor.setPanelText(mFactorFormat.format(getRace().getExplicitFactor()));
            } else {
                mButtonFactor.setPanelText(null);
            }
        }

        if (mButtonCourse != null) {
            mButtonCourse.setPanelText(getCourseName());
        }

        Wind wind = getRaceState().getWindFix();
        if (mButtonWind != null && wind != null) {
            String sensorData = getString(R.string.wind_panel, wind.getFrom().getDegrees(), wind.getKnots());
            mButtonWind.setPanelText(sensorData);
        }
    }

    private void checkStatus() {
        switch (getRace().getStatus()) {
        case UNSCHEDULED:
            changeVisibility(mButtonProcedure, false);
            changeVisibility(mButtonMode, false);
            changeVisibility(mButtonPathfinder, false);
            changeVisibility(mButtonTiming, false);
            changeVisibility(mButtonRaceGroup, false);
            changeVisibility(mButtonCourse, false);
            changeVisibility(mButtonWind, false);
            break;

        case PRESCHEDULED:
            changeVisibility(mButtonProcedure, false);
            changeVisibility(mButtonMode, false);
            changeVisibility(mButtonPathfinder, false);
            changeVisibility(mButtonTiming, false);
            changeVisibility(mButtonRaceGroup, false);
            changeVisibility(mButtonCourse, false);
            changeVisibility(mButtonWind, false);
            break;

        case SCHEDULED:
            changeVisibility(mButtonProcedure, false);
            changeVisibility(mButtonMode, false);
            changeVisibility(mButtonPathfinder, false);
            changeVisibility(mButtonTiming, false);
            changeVisibility(mButtonRaceGroup, false);
            changeVisibility(mButtonCourse, false);
            changeVisibility(mButtonWind, false);
            break;

        case STARTPHASE:
            changeVisibility(mButtonProcedure, true);
            changeVisibility(mButtonMode, true);
            changeVisibility(mButtonPathfinder, true);
            changeVisibility(mButtonTiming, true);
            changeVisibility(mButtonRaceGroup, true);
            changeVisibility(mButtonCourse, false);
            changeVisibility(mButtonWind, false);
            break;

        case RUNNING:
            changeVisibility(mButtonProcedure, true);
            changeVisibility(mButtonMode, true);
            changeVisibility(mButtonPathfinder, true);
            changeVisibility(mButtonTiming, true);
            changeVisibility(mButtonRaceGroup, true);
            changeVisibility(mButtonCourse, false);
            changeVisibility(mButtonWind, false);

            uncheckMarker(mButtonProcedure);
            uncheckMarker(mButtonMode);
            uncheckMarker(mButtonPathfinder);
            uncheckMarker(mButtonTiming);
            break;

        case FINISHING:
            changeVisibility(mButtonProcedure, true);
            changeVisibility(mButtonMode, true);
            changeVisibility(mButtonPathfinder, true);
            changeVisibility(mButtonTiming, true);
            changeVisibility(mButtonRaceGroup, true);
            changeVisibility(mButtonCourse, true);
            changeVisibility(mButtonWind, false);

            uncheckMarker(mButtonProcedure);
            uncheckMarker(mButtonMode);
            uncheckMarker(mButtonPathfinder);
            uncheckMarker(mButtonTiming);
            uncheckMarker(mButtonCourse);
            break;

        case FINISHED:
            changeVisibility(mButtonProcedure, false);
            changeVisibility(mButtonMode, false);
            changeVisibility(mButtonPathfinder, false);
            changeVisibility(mButtonTiming, false);
            changeVisibility(mButtonRaceGroup, false);
            changeVisibility(mButtonCourse, false);
            changeVisibility(mButtonWind, false);

            uncheckMarker(mButtonProcedure);
            uncheckMarker(mButtonMode);
            uncheckMarker(mButtonPathfinder);
            uncheckMarker(mButtonTiming);
            uncheckMarker(mButtonCourse);
            uncheckMarker(mButtonWind);
            break;

        default:
            changeVisibility(mButtonProcedure, true);
            changeVisibility(mButtonMode, true);
            changeVisibility(mButtonPathfinder, true);
            changeVisibility(mButtonTiming, true);
            changeVisibility(mButtonRaceGroup, true);
            changeVisibility(mButtonCourse, true);
            changeVisibility(mButtonWind, true);

            uncheckMarker(mButtonProcedure);
            uncheckMarker(mButtonMode);
            uncheckMarker(mButtonProcedure);
            uncheckMarker(mButtonTiming);
            uncheckMarker(mButtonCourse);
            uncheckMarker(mButtonWind);
            break;
        }
    }

    private void changeVisibility(PanelButton view, boolean showLock) {
        if (view != null) {
            view.setLock(showLock);
        }
    }

    private void uncheckMarker(PanelButton view) {
        if (isAdded()) {
            if (mButtonProcedure != null && !mButtonProcedure.equals(view)) {
                resetFragment(mButtonProcedure.isLocked(),
                        getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false),
                        StartProcedureFragment.class);
                mButtonProcedure.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonMode != null && !mButtonMode.equals(view)) {
                resetFragment(mButtonMode.isLocked(),
                        getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false), StartModeFragment.class);
                mButtonMode.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonPathfinder != null && !mButtonPathfinder.equals(view)) {
                resetFragment(mButtonPathfinder.isLocked(),
                        getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false),
                        GateStartPathFinderFragment.class);
                mButtonPathfinder.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonTiming != null && !mButtonTiming.equals(view)) {
                resetFragment(mButtonTiming.isLocked(),
                        getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false),
                        GateStartTimingFragment.class);
                mButtonTiming.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonFactor != null && !mButtonFactor.equals(view)) {
                resetFragment(mButtonFactor.isLocked(),
                        getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false), RaceFactorFragment.class);
                mButtonFactor.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonCourse != null && !mButtonCourse.equals(view)) {
                resetFragment(mButtonCourse.isLocked(),
                        getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false), CourseFragment.class);
                mButtonCourse.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonWind != null && !mButtonWind.equals(view)) {
                resetFragment(mButtonWind.isLocked(),
                        getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false), WindFragment.class);
                mButtonWind.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
        }
    }

    private class RaceStateChangedListener extends BaseRaceStateChangedListener {

        @Override
        public void onRacingProcedureChanged(ReadonlyRaceState state) {
            super.onRacingProcedureChanged(state);

            state.getRacingProcedure().addChangedListener(mProcedureListener);

            refreshPanel();
        }

        @Override
        public void onCourseDesignChanged(ReadonlyRaceState state) {
            super.onCourseDesignChanged(state);

            refreshPanel();
        }

        @Override
        public void onWindFixChanged(ReadonlyRaceState state) {
            super.onWindFixChanged(state);

            refreshPanel();
        }

        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            super.onStatusChanged(state);

            checkStatus();
        }
    }

    private class RaceProcedureChangedListener extends BaseRacingProcedureChangedListener {

        @Override
        public void onActiveFlagsChanged(ReadonlyRacingProcedure racingProcedure) {
            super.onActiveFlagsChanged(racingProcedure);

            refreshPanel();
        }
    }

    private class StartProcedureListener implements PanelButton.PanelButtonClick {

        private final String TAG = StartProcedureListener.class.getName();

        @Override
        public void onClick(PanelButton view) {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                    AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE);
            switch (view.toggleMarker()) {
            case PanelButton.LEVEL_NORMAL:
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                break;

            case PanelButton.LEVEL_TOGGLED:
                replaceFragment(StartProcedureFragment.newInstance(BaseFragment.START_MODE_PLANNED));
                break;

            default:
                ExLog.i(getActivity(), TAG, "Unknown return value");
                break;
            }
            view.disableToggle();
        }

        @Override
        public void onChangedSwitch(PanelButton view, boolean isChecked) {
            // no-op
        }
    }

    private class ButtonModeListener implements PanelButton.PanelButtonClick {

        private final String TAG = ButtonModeListener.class.getName();

        @Override
        public void onClick(PanelButton view) {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                    AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_MODE);
            switch (view.toggleMarker()) {
            case PanelButton.LEVEL_NORMAL:
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                break;

            case PanelButton.LEVEL_TOGGLED:
                replaceFragment(StartModeFragment.newInstance(StartModeFragment.START_MODE_PLANNED));
                break;

            default:
                ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            view.disableToggle();
        }

        @Override
        public void onChangedSwitch(PanelButton view, boolean isChecked) {
            // no-op
        }
    }

    private class ButtonPathfinderListener implements PanelButton.PanelButtonClick {

        private final String TAG = ButtonPathfinderListener.class.getName();

        @Override
        public void onClick(PanelButton view) {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                    AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_PATHFINDER);
            switch (view.toggleMarker()) {
            case PanelButton.LEVEL_NORMAL:
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                break;

            case PanelButton.LEVEL_TOGGLED:
                replaceFragment(
                        GateStartPathFinderFragment.newInstance(GateStartPathFinderFragment.START_MODE_PLANNED));
                break;

            default:
                ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            view.disableToggle();
        }

        @Override
        public void onChangedSwitch(PanelButton view, boolean isChecked) {
            // no-op
        }
    }

    private class ButtonTimingListener implements PanelButton.PanelButtonClick {

        private final String TAG = ButtonTimingListener.class.getName();

        @Override
        public void onClick(PanelButton view) {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                    AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_TIMING);
            switch (view.toggleMarker()) {
            case PanelButton.LEVEL_NORMAL:
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                break;

            case PanelButton.LEVEL_TOGGLED:
                replaceFragment(GateStartTimingFragment.newInstance(GateStartTimingFragment.START_MODE_PLANNED));
                break;

            default:
                ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            view.disableToggle();
        }

        @Override
        public void onChangedSwitch(PanelButton view, boolean isChecked) {
            // no-op
        }
    }

    private class ButtonRaceGroupListener implements PanelButton.PanelButtonClick {

        @Override
        public void onClick(PanelButton view) {
            // no-op
        }

        @Override
        public void onChangedSwitch(PanelButton view, boolean isChecked) {
            getRaceState().setAdditionalScoringInformationEnabled(MillisecondsTimePoint.now(), /* enable */isChecked,
                    AdditionalScoringInformationType.MAX_POINTS_DECREASE_MAX_SCORE);
        }
    }

    private class ButtonFactorListener implements PanelButton.PanelButtonClick {
        private final String TAG = ButtonFactorListener.class.getName();

        @Override
        public void onClick(PanelButton view) {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                    AppConstants.INTENT_ACTION_TOGGLE_FACTOR);
            switch (view.toggleMarker()) {
            case LEVEL_NORMAL:
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                break;

            case LEVEL_TOGGLED:
                replaceFragment(RaceFactorFragment.newInstance(BaseFragment.START_MODE_PLANNED));
                break;

            default:
                ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            view.disableToggle();
        }

        @Override
        public void onChangedSwitch(PanelButton view, boolean isChecked) {
            // no-op
        }
    }

    private class ButtonCourseListener implements PanelButton.PanelButtonClick {
        private final String TAG = ButtonCourseListener.class.getName();

        @Override
        public void onClick(PanelButton view) {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                    AppConstants.INTENT_ACTION_TOGGLE_COURSE);
            switch (view.toggleMarker()) {
            case LEVEL_NORMAL:
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                break;

            case LEVEL_TOGGLED:
                replaceFragment(CourseFragment.newInstance(BaseFragment.START_MODE_PLANNED, preferences));
                break;

            default:
                ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            view.disableToggle();
        }

        @Override
        public void onChangedSwitch(PanelButton view, boolean isChecked) {
            // no-op
        }
    }

    private class ButtonWindListener implements PanelButton.PanelButtonClick {
        private final String TAG = ButtonWindListener.class.getName();

        @Override
        public void onClick(PanelButton view) {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                    AppConstants.INTENT_ACTION_TOGGLE_WIND);
            switch (view.toggleMarker()) {
            case LEVEL_NORMAL:
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                break;

            case LEVEL_TOGGLED:
                replaceFragment(WindFragment.newInstance(BaseFragment.START_MODE_PLANNED));
                break;

            default:
                ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            view.disableToggle();
        }

        @Override
        public void onChangedSwitch(PanelButton view, boolean isChecked) {
            // no-op
        }
    }

    private class IntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppConstants.INTENT_ACTION_CLEAR_TOGGLE.equals(action)) {
                uncheckMarker(null);
            }

            if (AppConstants.INTENT_ACTION_TOGGLE.equals(action)) {
                if (intent.getExtras() != null) {
                    String data = intent.getExtras().getString(AppConstants.INTENT_ACTION_EXTRA);
                    switch (data) {
                    case AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE:
                        uncheckMarker(mButtonProcedure);
                        break;
                    case AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_MODE:
                        uncheckMarker(mButtonMode);
                        break;
                    case AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_PATHFINDER:
                        uncheckMarker(mButtonPathfinder);
                        break;
                    case AppConstants.INTENT_ACTION_TOGGLE_PROCEDURE_MORE_TIMING:
                        uncheckMarker(mButtonTiming);
                        break;
                    case AppConstants.INTENT_ACTION_TOGGLE_FACTOR:
                        uncheckMarker(mButtonFactor);
                        break;
                    case AppConstants.INTENT_ACTION_TOGGLE_COURSE:
                        uncheckMarker(mButtonCourse);
                        break;
                    case AppConstants.INTENT_ACTION_TOGGLE_WIND:
                        uncheckMarker(mButtonWind);
                        break;
                    default:
                        uncheckMarker(null);
                        break;
                    }
                }
            }

            if (AppConstants.INTENT_ACTION_UPDATE_SCREEN.equals(action)) {
                refreshPanel();
            }
        }
    }
}
