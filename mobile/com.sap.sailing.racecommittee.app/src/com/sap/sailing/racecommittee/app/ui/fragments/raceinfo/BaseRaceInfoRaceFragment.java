package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.utils.CourseDesignerChooser;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.List;

public abstract class BaseRaceInfoRaceFragment<ProcedureType extends RacingProcedure> extends RaceFragment {

    private final ProcedureChangedListener procedureListener;
    protected RaceInfoListener infoListener;
    private View raceHeader;
    private View startProcedure;
    private View startProcedureLock;
    private View startMode;
    private View startModeLock;
    private View course;
    private View courseLock;
    private View wind;
    private View windLock;
    private View abandonFlags;
    private View recallFlags;
    private View postponeFlags;
    private View courseFlags;
    private View moreFlags;
    private TextView courseValue;
    private TextView startProcedureValue;
    private ImageView courseIcon;
    private ImageView startModeFlag;
    private UpdateUiReceiver mReceiver;
    private FlagPoleCache flagPoleCache;

    public BaseRaceInfoRaceFragment() {
        procedureListener = new ProcedureChangedListener();
        flagPoleCache = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null) {
            raceHeader = getView().findViewById(R.id.race_content_header);
            if (raceHeader != null) {
                raceHeader.setOnClickListener(new RaceHeaderClick());
            }

            startProcedure = getView().findViewById(R.id.start_procedure);
            if (startProcedure != null) {
                startProcedure.setOnClickListener(new StartProcedureClick());
            }
            startProcedureLock = getView().findViewById(R.id.start_procedure_lock);
            startProcedureValue = (TextView) getView().findViewById(R.id.start_procedure_value);

            startMode = getView().findViewById(R.id.start_mode);
            if (startMode != null) {
                startMode.setOnClickListener(new StartModeClick());
            }
            startModeLock = getView().findViewById(R.id.start_mode_lock);
            startModeFlag = (ImageView) getView().findViewById(R.id.start_mode_flag);

            course = getView().findViewById(R.id.course);
            if (course != null) {
                course.setOnClickListener(new CourseClick());
            }
            courseLock = getView().findViewById(R.id.course_lock);
            courseIcon = (ImageView) getView().findViewById(R.id.course_icon);
            courseValue = (TextView) getView().findViewById(R.id.course_value);

            wind = getView().findViewById(R.id.wind);
            if (wind != null) {
                wind.setOnClickListener(new WindClick());
            }
            windLock = getView().findViewById(R.id.wind_lock);

            abandonFlags = getView().findViewById(R.id.abandon_flags);
            if (abandonFlags != null) {
                abandonFlags.setOnClickListener(new AbandonFlagsClick());
            }

            recallFlags = getView().findViewById(R.id.recall_flags);
            if (recallFlags != null) {
                recallFlags.setOnClickListener(new RecallFlagsClick());
            }

            postponeFlags = getView().findViewById(R.id.postpone_flags);
            if (postponeFlags != null) {
                postponeFlags.setOnClickListener(new PostponeFlagsClick());
            }

            courseFlags = getView().findViewById(R.id.course_flags);
            if (courseFlags != null) {
                courseFlags.setOnClickListener(new CourseFlagsClick());
            }

            moreFlags = getView().findViewById(R.id.more_flags);
            if (moreFlags != null) {
                moreFlags.setOnClickListener(new MoreFlagsClick());
            }
        }

        sendIntent(R.string.intent_update_ui);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof RaceInfoListener) {
            this.infoListener = (RaceInfoListener) activity;
        } else {
            throw new UnsupportedOperationException(String.format(
                    "%s must implement %s",
                    activity,
                    RaceInfoListener.class.getName()));
        }

        mReceiver = new UpdateUiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.intent_update_ui));
        filter.addAction(getString(R.string.intent_uncheck_all));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setupUi();
        getRacingProcedure().addChangedListener(procedureListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        getRacingProcedure().removeChangedListener(procedureListener);
    }

    @Override
    public void notifyTick() {
        super.notifyTick();

        setLockUnlock();
    }

    private void setLockUnlock() {
        changeVisibility(startProcedureLock, View.GONE);
        changeVisibility(startModeLock, View.GONE);
        changeVisibility(courseLock, View.GONE);
        changeVisibility(windLock, View.GONE);

        changeVisibility(postponeFlags, View.VISIBLE);
        changeVisibility(abandonFlags, View.VISIBLE);
        changeVisibility(recallFlags, View.VISIBLE);
        changeVisibility(moreFlags, View.VISIBLE);

        switch (getRace().getStatus()) {
            case UNSCHEDULED:
                break;

            case SCHEDULED:
            case STARTPHASE:
                changeVisibility(abandonFlags, View.GONE);
                changeVisibility(recallFlags, View.GONE);
                changeVisibility(moreFlags, View.GONE);
                uncheckMarker(abandonFlags);
                uncheckMarker(recallFlags);
                uncheckMarker(moreFlags);
                break;

            case RUNNING:
                changeVisibility(startProcedureLock, View.VISIBLE);
                changeVisibility(startModeLock, View.VISIBLE);
                break;

            case FINISHING:
                changeVisibility(startProcedureLock, View.VISIBLE);
                changeVisibility(startModeLock, View.VISIBLE);
                changeVisibility(courseLock, View.VISIBLE);
                break;

            case FINISHED:
                break;

            default:
                break;
        }
    }

    private void changeVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    protected ProcedureType getRacingProcedure() {
        return getRaceState().getTypedRacingProcedure();
    }

    protected void showCourseDesignDialog() {
        RaceDialogFragment fragment = CourseDesignerChooser.choose(preferences, getRace());
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
        Bundle args = getRecentArguments();
        if (fragment.getArguments() != null) {
            args.putAll(fragment.getArguments());
        }
        fragment.setArguments(args);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.race_frame, fragment)
                .commit();
        sendIntent(R.string.intent_update_ui);
    }

    private void updateUi() {

        if (courseIcon != null) {
            courseIcon.setImageDrawable(getResources().getDrawable(R.drawable.course_updown_64dp));
        }

        if (courseValue != null) {
            courseValue.setText(getRaceState().getCourseDesign().getName());
        }

        if (startProcedureValue != null) {
            startProcedureValue.setText(getRacingProcedure().getType().toString());
        }

        if (startModeFlag != null) {
            try {
                if (startMode != null) {
                    startMode.setVisibility(View.VISIBLE);
                }
                RRS26RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                startModeFlag.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), procedure.getStartModeFlag().name(), 48));
            } catch (Exception ex) {
                if (startMode != null) {
                    startMode.setVisibility(View.GONE);
                }
            }
        }
    }

    private void uncheckMarker(View v) {
        if (!v.equals(startProcedure)) {
            setMarkerLevel(startProcedure, R.id.start_procedure_marker, 0);
        }

        if (!v.equals(startMode)) {
            setMarkerLevel(startMode, R.id.start_mode_marker, 0);
        }

        if (!v.equals(course)) {
            setMarkerLevel(course, R.id.course_marker, 0);
        }

        if (!v.equals(wind)) {
            setMarkerLevel(wind, R.id.wind_marker, 0);
        }

        if (!v.equals(abandonFlags)) {
            setMarkerLevel(abandonFlags, R.id.abandon_flags_marker, 0);
        }

        if (!v.equals(recallFlags)) {
            setMarkerLevel(recallFlags, R.id.recall_flags_marker, 0);
        }

        if (!v.equals(postponeFlags)) {
            setMarkerLevel(postponeFlags, R.id.postpone_flags_marker, 0);
        }

        if (!v.equals(courseFlags)) {
            setMarkerLevel(courseFlags, R.id.course_flags_marker, 0);
        }

        if (!v.equals(moreFlags)) {
            setMarkerLevel(moreFlags, R.id.more_flags_marker, 0);
        }
    }

    private int toggleMarker(View v, @IdRes int resId) {
        int retValue = -1;
        ImageView view = (ImageView) v.findViewById(resId);
        if (view != null) {
            Drawable drawable = view.getDrawable();
            if (drawable != null) {
                retValue = setMarkerLevel(v, resId, 1 - drawable.getLevel());
            }
        }

        return retValue;
    }

    private int setMarkerLevel(View v, @IdRes int resId, int level) {
        int retValue = -1;
        ImageView view = (ImageView) v.findViewById(resId);
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            Drawable drawable = view.getDrawable();
            if (drawable != null) {
                drawable.setLevel(level);
                switch (drawable.getLevel()) {
                    case 1:
                        if (layoutParams != null) {
                            layoutParams.height = getResources().getDimensionPixelSize(R.dimen.thick_line);
                            view.invalidate();
                        }
                        v.setBackgroundColor(getResources().getColor(R.color.sap_gray_black_20));
                        break;

                    default:
                        if (layoutParams != null) {
                            layoutParams.height = getResources().getDimensionPixelSize(R.dimen.thin_line);
                            view.invalidate();
                        }
                        v.setBackgroundColor(getResources().getColor(R.color.sap_gray));
                        break;
                }
                retValue = drawable.getLevel();
            }
        }
        return retValue;
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

    private class RaceHeaderClick implements View.OnClickListener {

        private final String TAG = RaceHeaderClick.class.getName();

        public void onClick(View v) {
            uncheckMarker(v);
            switch (toggleMarker(v, R.id.bottom_line)) {
                case 0:
                    replaceFragment(RaceFlagViewerFragment.newInstance());
                    break;

                case 1:
                    replaceFragment(StartTimeFragment.newInstance(2));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
                    break;
            }
        }
    }

    private class StartProcedureClick implements View.OnClickListener {

        private final String TAG = StartProcedureClick.class.getName();

        @Override
        public void onClick(View v) {
            if (startProcedureLock == null || startProcedureLock.getVisibility() == View.GONE) {
                uncheckMarker(v);
                switch (toggleMarker(v, R.id.start_procedure_marker)) {
                    case 0:
                        replaceFragment(RaceFlagViewerFragment.newInstance());
                        break;

                    case 1:
                        replaceFragment(StartProcedureFragment.newInstance(1));
                        break;

                    default:
                        ExLog.i(getActivity(), TAG, "Unknown return value");
                        break;
                }
            }
        }
    }

    private class StartModeClick implements View.OnClickListener {

        private final String TAG = StartModeClick.class.getName();

        @Override
        public void onClick(View v) {
            if (startModeLock == null || startModeLock.getVisibility() == View.GONE) {
                uncheckMarker(v);
                switch (toggleMarker(v, R.id.start_mode_marker)) {
                    case 0:
                        replaceFragment(RaceFlagViewerFragment.newInstance());
                        break;

                    case 1:
                        replaceFragment(StartModeFragment.newInstance(1));
                        break;

                    default:
                        ExLog.i(getActivity(), TAG, "Unknown return value");
                }
            }
        }
    }

    private class CourseClick implements View.OnClickListener {

        private final String TAG = CourseClick.class.getName();

        @Override
        public void onClick(View v) {
            if (courseLock == null || courseLock.getVisibility() == View.GONE) {
                uncheckMarker(v);
                switch (toggleMarker(v, R.id.course_marker)) {
                    case 0:
                        replaceFragment(RaceFlagViewerFragment.newInstance());
                        break;

                    case 1:
                        replaceFragment(CourseFragmentHelper.newInstance(1));
                        break;

                    default:
                        ExLog.i(getActivity(), TAG, "Unknown return value");
                }
            }
        }
    }

    private class WindClick implements View.OnClickListener {

        private final String TAG = WindClick.class.getName();

        @Override
        public void onClick(View v) {
            if (windLock == null || windLock.getVisibility() == View.GONE) {
                uncheckMarker(v);
                switch (toggleMarker(v, R.id.wind_marker)) {
                    case 0:
                        replaceFragment(RaceFlagViewerFragment.newInstance());
                        break;

                    case 1:
                        replaceFragment(WindFragment.newInstance(1));
                        break;

                    default:
                        ExLog.i(getActivity(), TAG, "Unknown return value");
                }
            }
        }
    }

    private class AbandonFlagsClick implements View.OnClickListener {

        private final String TAG = AbandonFlagsClick.class.getName();

        @Override
        public void onClick(View v) {
            uncheckMarker(v);
            switch (toggleMarker(v, R.id.abandon_flags_marker)) {
                case 0:
                    replaceFragment(RaceFlagViewerFragment.newInstance());
                    break;

                case 1:
                    replaceFragment(AbortFlagsFragment.newInstance(Flags.NOVEMBER));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
        }
    }

    private class RecallFlagsClick implements View.OnClickListener {

        private final String TAG = RecallFlagsClick.class.getName();

        @Override
        public void onClick(View v) {
            uncheckMarker(v);
            switch (toggleMarker(v, R.id.recall_flags_marker)) {
                case 0:
                    replaceFragment(RaceFlagViewerFragment.newInstance());
                    break;

                case 1:
                    replaceFragment(RecallFlagsFragment.newInstance());
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
        }
    }

    private class PostponeFlagsClick implements View.OnClickListener {

        private final String TAG = PostponeFlagsClick.class.getName();

        @Override
        public void onClick(View v) {
            uncheckMarker(v);
            switch (toggleMarker(v, R.id.postpone_flags_marker)) {
                case 0:
                    replaceFragment(RaceFlagViewerFragment.newInstance());
                    break;

                case 1:
                    replaceFragment(AbortFlagsFragment.newInstance(Flags.AP));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
        }
    }

    private class CourseFlagsClick implements View.OnClickListener {

        private final String TAG = CourseFlagsClick.class.getName();

        @Override
        public void onClick(View v) {
            uncheckMarker(v);
            switch (toggleMarker(v, R.id.course_flags_marker)) {
                case 0:
                    replaceFragment(RaceFlagViewerFragment.newInstance());
                    break;

                case 1:
                    replaceFragment(EmptyFragment.newInstance());
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
        }
    }

    private class MoreFlagsClick implements View.OnClickListener {

        private final String TAG = MoreFlagsClick.class.getName();

        @Override
        public void onClick(View v) {
            uncheckMarker(v);

            switch (toggleMarker(v, R.id.more_flags_marker)) {
                case 0:
                    replaceFragment(RaceFlagViewerFragment.newInstance());
                    break;

                case 1:
                    replaceFragment(MoreFlagsFragment.newInstance());
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
        }
    }

    private class UpdateUiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null && intent != null) {
                if (intent.getAction().equals(getString(R.string.intent_update_ui))) {
                    updateUi();
                }

                if (intent.getAction().equals(getString(R.string.intent_uncheck_all))) {
                    uncheckMarker(new View(getActivity()));
                }
            }
        }
    }
}
