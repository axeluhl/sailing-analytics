package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

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
import com.sap.sailing.racecommittee.app.ui.NavigationEvents;
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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SetupPanelFragment extends BasePanelFragment implements NavigationEvents.NavigationListener {

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

    private static final String STATE_CURRENT_FLAG = "state-current-flag";
    private Map<Integer, Boolean> flagStates = new HashMap<>();

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout;
        if (getArguments()!=null && getArguments().getInt(ARGS_PAGE, 0) == 1) {
            layout = inflater.inflate(R.layout.race_panel_setup_hor_2, container, false);
        } else {
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
        filter.addAction(AppConstants.INTENT_ACTION_UPDATE_SCREEN);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mReceiver, filter);
        View view = getView();
        if (view != null) {
            for (Integer id : flagStates.keySet()) {
                final View v = ViewHelper.get(view, id);
                if (v instanceof PanelButton) {
                    updateOneMarker((PanelButton) v, flagStates.get(id) == Boolean.TRUE);
                }
            }
        }

    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        NavigationEvents.INSTANCE.subscribeFragmentAttachment(this);
    }

    @Override
    public void onDetach() {
        NavigationEvents.INSTANCE.unSubscribeFragmentAttachment(this);
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFactorFormat = new DecimalFormat(requireActivity().getString(R.string.race_factor_format));
        if (savedInstanceState != null) {
            flagStates.putAll((Map<Integer, Boolean>) savedInstanceState.getSerializable(STATE_CURRENT_FLAG));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        getRaceState().removeChangedListener(mStateListener);
        getRaceState().getRacingProcedure().removeChangedListener(mProcedureListener);

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mReceiver);
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
            case PRESCHEDULED:
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

    private void updateOneMarker(PanelButton view, boolean checked) {
        final int level = checked ? LEVEL_TOGGLED : LEVEL_NORMAL;
        if (isAdded()) {
            if (view != null) {
                flagStates.put(view.getId(), checked);
                view.setMarkerLevel(level);
            }
        }
    }

    private void uncheckMarker(PanelButton view) {
        if (isAdded()) {
            final FragmentActivity activity = requireActivity();
            if (mButtonProcedure != null && !mButtonProcedure.equals(view)) {
                resetFragment(mButtonProcedure.isLocked(),
                        getFrameId(activity, R.id.race_edit, R.id.race_content, false),
                        StartProcedureFragment.class);
                mButtonProcedure.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonMode != null && !mButtonMode.equals(view)) {
                resetFragment(mButtonMode.isLocked(),
                        getFrameId(activity, R.id.race_edit, R.id.race_content, false), StartModeFragment.class);
                mButtonMode.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonPathfinder != null && !mButtonPathfinder.equals(view)) {
                resetFragment(mButtonPathfinder.isLocked(),
                        getFrameId(activity, R.id.race_edit, R.id.race_content, false),
                        GateStartPathFinderFragment.class);
                mButtonPathfinder.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonTiming != null && !mButtonTiming.equals(view)) {
                resetFragment(mButtonTiming.isLocked(),
                        getFrameId(activity, R.id.race_edit, R.id.race_content, false),
                        GateStartTimingFragment.class);
                mButtonTiming.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonFactor != null && !mButtonFactor.equals(view)) {
                resetFragment(mButtonFactor.isLocked(),
                        getFrameId(activity, R.id.race_edit, R.id.race_content, false), RaceFactorFragment.class);
                mButtonFactor.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonCourse != null && !mButtonCourse.equals(view)) {
                resetFragment(mButtonCourse.isLocked(),
                        getFrameId(activity, R.id.race_edit, R.id.race_content, false), CourseFragment.class);
                mButtonCourse.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
            if (mButtonWind != null && !mButtonWind.equals(view)) {
                resetFragment(mButtonWind.isLocked(),
                        getFrameId(activity, R.id.race_edit, R.id.race_content, false), WindFragment.class);
                mButtonWind.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
        }
    }

    @Override
    public void onFragmentAttach(Fragment fragment) {
        if (fragment instanceof StartProcedureFragment) {
            uncheckMarker(mButtonProcedure);
        } else if (fragment instanceof StartModeFragment) {
            uncheckMarker(mButtonMode);
        } else if (fragment instanceof GateStartPathFinderFragment) {
            uncheckMarker(mButtonPathfinder);
        } else if (fragment instanceof GateStartTimingFragment) {
            uncheckMarker(mButtonTiming);
        } else if (fragment instanceof RaceFactorFragment) {
            uncheckMarker(mButtonFactor);
        } else if (fragment instanceof CourseFragment) {
            uncheckMarker(mButtonCourse);
        } else if (fragment instanceof WindFragment) {
            uncheckMarker(mButtonWind);
        }
    }

    @Override
    public void onFragmentDetach(Fragment fragment) {
        if (fragment instanceof StartProcedureFragment) {
            updateOneMarker(mButtonProcedure, false);
        } else if (fragment instanceof StartModeFragment) {
            updateOneMarker(mButtonMode, false);
        } else if (fragment instanceof GateStartPathFinderFragment) {
            updateOneMarker(mButtonPathfinder, false);
        } else if (fragment instanceof GateStartTimingFragment) {
            updateOneMarker(mButtonTiming, false);
        } else if (fragment instanceof RaceFactorFragment) {
            updateOneMarker(mButtonFactor, false);
        } else if (fragment instanceof CourseFragment) {
            updateOneMarker(mButtonCourse, false);
        } else if (fragment instanceof WindFragment) {
            updateOneMarker(mButtonWind, false);
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
            int toggle = view.toggleMarker();
            flagStates.put(view.getId(), toggle == PanelButton.LEVEL_TOGGLED);
            switch (toggle) {
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
            int toggle = view.toggleMarker();
            flagStates.put(view.getId(), toggle == PanelButton.LEVEL_TOGGLED);
            switch (toggle) {
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
            int toggle = view.toggleMarker();
            flagStates.put(view.getId(), toggle == PanelButton.LEVEL_TOGGLED);
            switch (toggle) {
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
            int toggle = view.toggleMarker();
            flagStates.put(view.getId(), toggle == PanelButton.LEVEL_TOGGLED);
            switch (toggle) {
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
            int toggle = view.toggleMarker();
            flagStates.put(view.getId(), toggle == PanelButton.LEVEL_TOGGLED);
            switch (toggle) {
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
            int toggle = view.toggleMarker();
            flagStates.put(view.getId(), toggle == PanelButton.LEVEL_TOGGLED);
            switch (toggle) {
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
            int toggle = view.toggleMarker();
            flagStates.put(view.getId(), toggle == PanelButton.LEVEL_TOGGLED);
            switch (toggle) {
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

            if (AppConstants.INTENT_ACTION_UPDATE_SCREEN.equals(action)) {
                refreshPanel();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_CURRENT_FLAG, (Serializable) flagStates);
    }
}
