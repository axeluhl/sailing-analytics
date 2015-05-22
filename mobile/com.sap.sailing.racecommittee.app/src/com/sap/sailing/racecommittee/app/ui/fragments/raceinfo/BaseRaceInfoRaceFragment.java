package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.widget.TextView;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.FlagPanelFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.SetupPanelFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.TimePanelFragment;
import com.sap.sailing.racecommittee.app.ui.utils.CourseDesignerChooser;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.List;

public abstract class BaseRaceInfoRaceFragment<ProcedureType extends RacingProcedure> extends RaceFragment {

    private final RaceStateChangedListener raceStateChangedListener;
    private final ProcedureChangedListener procedureListener;

    protected RaceInfoListener infoListener;
    private FlagPoleCache flagPoleCache;

    public BaseRaceInfoRaceFragment() {
        raceStateChangedListener = new RaceStateChangedListener();
        procedureListener = new ProcedureChangedListener();

        flagPoleCache = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        replaceFragment(SetupPanelFragment.newInstance(getArguments()), R.id.race_panel_left);
        replaceFragment(FlagPanelFragment.newInstance(getArguments()), R.id.race_panel_right);
        replaceFragment(TimePanelFragment.newInstance(getArguments()), R.id.race_panel_top);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof RaceInfoListener) {
            this.infoListener = (RaceInfoListener) activity;
        } else {
            throw new UnsupportedOperationException(String.format("%s must implement %s", activity, RaceInfoListener.class.getName()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setupUi();

        getRaceState().addChangedListener(raceStateChangedListener);
        getRacingProcedure().addChangedListener(procedureListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        getRaceState().removeChangedListener(raceStateChangedListener);
        getRacingProcedure().removeChangedListener(procedureListener);
    }

    protected ProcedureType getRacingProcedure() {
        return getRaceState().getTypedRacingProcedure();
    }

    protected void showCourseDesignDialog() {
        RaceDialogFragment fragment = CourseDesignerChooser.choose(getRace());
        fragment.setArguments(getRecentArguments());
        fragment.show(getFragmentManager(), "courseDesignDialogFragment");
    }

    protected abstract void setupUi();

    protected boolean updateFlagChangesCountdown(TextView targetView) {
        if (flagPoleCache == null) {
            ExLog.i(getActivity(), BaseRaceInfoRaceFragment.class.getSimpleName(), "Refilling next-flag cache.");
            TimePoint now = MillisecondsTimePoint.now();
            TimePoint startTime = getRaceState().getStartTime();
            FlagPoleState flagState = getRaceState().getRacingProcedure().getActiveFlags(startTime, now);
            List<FlagPole> flagChanges = flagState.computeUpcomingChanges();
            if (!flagChanges.isEmpty()) {
                TimePoint changeAt = flagState.getNextStateValidFrom();
                FlagPole changePole = FlagPoleState.getMostInterestingFlagPole(flagChanges);

                renderFlagChangesCountdown(targetView, changeAt, changePole);
                flagPoleCache = new FlagPoleCache(changePole, changeAt);
                return true;
            } else {
                flagPoleCache = new FlagPoleCache();
            }
            return false;
        } else if (flagPoleCache.hasNextFlag) {
            TimePoint changeAt = flagPoleCache.timePoint;
            FlagPole changePole = flagPoleCache.flagPole;

            renderFlagChangesCountdown(targetView, changeAt, changePole);
            return true;
        }
        return false;
    }

    protected void onIndividualRecallChanged(boolean displayed) {
        // overwrite in derived fragments
    }

    private void renderFlagChangesCountdown(TextView targetView, TimePoint changeAt, FlagPole changePole) {
        long millisecondsTillChange = TimeUtils.timeUntil(changeAt);
        int formatTextResourceId = changePole.isDisplayed() ? R.string.race_startphase_countdown_mode_display :
                R.string.race_startphase_countdown_mode_remove;
        StringBuilder flagName = new StringBuilder();
        flagName.append(changePole.getUpperFlag().toString());
        if (changePole.getLowerFlag() != Flags.NONE) {
            flagName.append("|");
            flagName.append(changePole.getLowerFlag().toString());
        }
        targetView.setText(getString(formatTextResourceId, TimeUtils.formatDurationUntil(millisecondsTillChange), flagName));
    }

    protected void replaceFragment(RaceFragment fragment) {
        replaceFragment(fragment, R.id.race_frame);
    }

    protected void replaceFragment(RaceFragment fragment, @IdRes int id) {
        if (getView() != null && getView().findViewById(id) != null) {
            Bundle args = getRecentArguments();
            if (fragment.getArguments() != null) {
                args.putAll(fragment.getArguments());
            }
            fragment.setArguments(args);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(id, fragment);
            transaction.commit();
        }
    }

    private class FlagPoleCache {
        public final FlagPole flagPole;
        public final TimePoint timePoint;
        public final boolean hasNextFlag;

        public FlagPoleCache() {
            this(null, null, false);
        }

        public FlagPoleCache(FlagPole flagPole, TimePoint timePoint) {
            this(flagPole, timePoint, true);
        }

        private FlagPoleCache(FlagPole flagPole, TimePoint timePoint, boolean hasNextFlag) {
            this.flagPole = flagPole;
            this.timePoint = timePoint;
            this.hasNextFlag = hasNextFlag;
        }
    }

    private class RaceStateChangedListener extends BaseRaceStateChangedListener {

        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            super.onStatusChanged(state);

            switch (state.getStatus()) {
            case UNSCHEDULED:
                RacingActivity activity = (RacingActivity) getActivity();
                if (activity != null) {
                    activity.onRaceItemClicked(getRace());
                }
                break;

            default:
                break;
            }
        }

        @Override
        public void onFinishedTimeChanged(ReadonlyRaceState state) {
            super.onFinishedTimeChanged(state);

            RacingActivity activity = (RacingActivity) getActivity();
            if (activity != null) {
                activity.onRaceItemClicked(getRace());
            }
        }
    }

    private class ProcedureChangedListener extends BaseRacingProcedureChangedListener {
        @Override
        public void onActiveFlagsChanged(ReadonlyRacingProcedure racingProcedure) {
            setupUi();
            flagPoleCache = null;
        }

        @Override
        public void onIndividualRecallDisplayed(ReadonlyRacingProcedure racingProcedure) {
            onIndividualRecallChanged(true);
        }

        @Override
        public void onIndividualRecallRemoved(ReadonlyRacingProcedure racingProcedure) {
            onIndividualRecallChanged(false);
        }
    }
}
