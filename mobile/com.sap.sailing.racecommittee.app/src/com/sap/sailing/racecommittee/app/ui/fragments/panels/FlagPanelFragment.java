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
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.AbortFlagsFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.EmptyFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.MoreFlagsFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RecallFlagsFragment;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;

public class FlagPanelFragment extends BasePanelFragment {

    private RaceStateChangedListener mStateListener;
    private IntentReceiver mReceiver;

    // Abandon Toggle
    private View mAbandonFlags;
    private View mAbandonLock;
    private View mAbandonLayer;

    // Recall Toggle
    private View mRecallFlags;
    private View mRecallLock;
    private View mRecallLayer;

    // Postpone Toggle
    private View mPostponeFlags;
    private View mPostponeLock;
    private View mPostponeLayer;

    // Course Toggle
    private View mCourseFlags;
    private View mCourseLock;

    // Blue First
    private View mBlueFirstFlag;
    private View mBlueFirstLock;

    // Blue Last
    private View mBlueLastFlag;
    private View mBlueLastLock;
    private TextView mBlueLastText;

    public FlagPanelFragment() {
        mReceiver = new IntentReceiver();
    }

    public static FlagPanelFragment newInstance(Bundle args) {
        FlagPanelFragment fragment = new FlagPanelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_panel_flags, container, false);

        mAbandonFlags = ViewHelper.get(layout, R.id.abandon_flags);
        if (mAbandonFlags != null) {
            mAbandonFlags.setOnClickListener(new AbandonFlagsClick());
        }
        mAbandonLock = ViewHelper.get(layout, R.id.abandon_flags_lock);
        mAbandonLayer = ViewHelper.get(layout, R.id.abandon_flags_layer);

        mRecallFlags = ViewHelper.get(layout, R.id.recall_flags);
        if (mRecallFlags != null) {
            mRecallFlags.setOnClickListener(new RecallFlagsClick());
        }
        mRecallLock = ViewHelper.get(layout, R.id.recall_flags_lock);
        mRecallLayer = ViewHelper.get(layout, R.id.recall_flags_layer);

        mPostponeFlags = ViewHelper.get(layout, R.id.postpone_flags);
        if (mPostponeFlags != null) {
            mPostponeFlags.setOnClickListener(new PostponeFlagsClick());
        }
        mPostponeLock = ViewHelper.get(layout, R.id.postpone_flags_lock);
        mPostponeLayer = ViewHelper.get(layout, R.id.postpone_flags_layer);

        mCourseFlags = ViewHelper.get(layout, R.id.course_flags);
        if (mCourseFlags != null) {
            mCourseFlags.setOnClickListener(new CourseFlagsClick());
        }
        mCourseLock = ViewHelper.get(layout, R.id.course_flags_lock);

        mBlueFirstFlag = ViewHelper.get(layout, R.id.blue_first_flags);
        if (mBlueFirstFlag != null) {
            mBlueFirstFlag.setOnClickListener(new BlueFirstFlagClick());
        }
        mBlueFirstLock = ViewHelper.get(layout, R.id.blue_first_flags_lock);

        mBlueLastFlag = ViewHelper.get(layout, R.id.blue_last_flags);
        if (mBlueLastFlag != null) {
            mBlueLastFlag.setOnClickListener(new BlueLastFlagClick());
        }
        mBlueLastLock = ViewHelper.get(layout, R.id.blue_last_flags_lock);
        mBlueLastText = ViewHelper.get(layout, R.id.blue_last_flags_text);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        checkStatus();

        mStateListener = new RaceStateChangedListener();

        getRaceState().addChangedListener(mStateListener);

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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void checkStatus() {
        changeVisibility(mPostponeLock, mPostponeLayer, View.GONE);
        changeVisibility(mAbandonLock, mAbandonLayer, View.GONE);
        changeVisibility(mRecallLock, null, View.GONE);
        changeVisibility(mCourseLock, null, View.GONE);
        changeVisibility(mBlueFirstLock, null, View.GONE);
        changeVisibility(mBlueLastLock, null, View.GONE);

        switch (getRace().getStatus()) {
            case UNSCHEDULED:
                changeVisibility(mPostponeFlags, null, View.VISIBLE);
                changeVisibility(mAbandonFlags, null, View.VISIBLE);
                changeVisibility(mRecallFlags, null, View.VISIBLE);
                changeVisibility(mCourseFlags, null, View.GONE);
                changeVisibility(mBlueFirstFlag, null, View.VISIBLE);
                changeVisibility(mBlueLastFlag, null, View.GONE);

                uncheckMarker(mCourseFlags);
                break;

            case PRESCHEDULED:
                changeVisibility(mPostponeFlags, null, View.VISIBLE);
                changeVisibility(mAbandonFlags, null, View.GONE);
                changeVisibility(mRecallFlags, null, View.GONE);
                changeVisibility(mCourseFlags, null, View.GONE);
                changeVisibility(mBlueFirstFlag, null, View.GONE);
                changeVisibility(mBlueLastFlag, null, View.GONE);

                uncheckMarker(mAbandonFlags);
                uncheckMarker(mCourseFlags);
                uncheckMarker(mBlueFirstFlag);
                uncheckMarker(mBlueLastFlag);
                break;

            case SCHEDULED:
            case STARTPHASE:
                changeVisibility(mPostponeFlags, null, View.VISIBLE);
                changeVisibility(mAbandonFlags, null, View.GONE);
                changeVisibility(mRecallFlags, null, View.GONE);
                changeVisibility(mCourseFlags, null, View.GONE);
                changeVisibility(mBlueFirstFlag, null, View.GONE);
                changeVisibility(mBlueLastFlag, null, View.GONE);

                uncheckMarker(mAbandonFlags);
                uncheckMarker(mRecallFlags);
                uncheckMarker(mBlueFirstFlag);
                uncheckMarker(mBlueLastFlag);
                break;

            case RUNNING:
                changeVisibility(mPostponeLock, mPostponeLayer, View.VISIBLE);

                changeVisibility(mPostponeFlags, null, View.VISIBLE);
                changeVisibility(mAbandonFlags, null, View.VISIBLE);
                changeVisibility(mRecallFlags, null, View.VISIBLE);
                changeVisibility(mCourseFlags, null, View.GONE);
                changeVisibility(mBlueFirstFlag, null, View.VISIBLE);
                changeVisibility(mBlueLastFlag, null, View.GONE);

                uncheckMarker(mCourseFlags);
                break;

            case FINISHING:
                changeVisibility(mPostponeFlags, null, View.GONE);
                changeVisibility(mAbandonFlags, null, View.VISIBLE);
                changeVisibility(mRecallFlags, null, View.GONE);
                changeVisibility(mCourseFlags, null, View.GONE);
                changeVisibility(mBlueFirstFlag, null, View.GONE);
                changeVisibility(mBlueLastFlag, null,
                        preferences.getRacingProcedureIsResultEntryEnabled(getRaceState().getRacingProcedure().getType())
                                ? View.VISIBLE : View.GONE);

                uncheckMarker(mRecallFlags);
                uncheckMarker(mCourseFlags);
                uncheckMarker(mBlueFirstFlag);
                uncheckMarker(mBlueLastFlag);
                break;

            case FINISHED:
                changeVisibility(mPostponeFlags, null, View.GONE);
                changeVisibility(mAbandonFlags, null, View.GONE);
                changeVisibility(mRecallFlags, null, View.GONE);
                changeVisibility(mCourseFlags, null, View.GONE);
                changeVisibility(mBlueFirstFlag, null, View.GONE);
                changeVisibility(mBlueLastFlag, null, View.GONE);

                uncheckMarker(mCourseFlags);
                break;

            default:
                changeVisibility(mPostponeFlags, mPostponeLayer, View.GONE);
                changeVisibility(mAbandonFlags, null, View.GONE);
                changeVisibility(mRecallFlags, null, View.GONE);
                changeVisibility(mCourseFlags, null, View.GONE);
                changeVisibility(mBlueFirstFlag, null, View.GONE);
                changeVisibility(mBlueLastFlag, null, View.GONE);

                uncheckMarker(mPostponeFlags);
                uncheckMarker(mAbandonFlags);
                uncheckMarker(mRecallFlags);
                uncheckMarker(mCourseFlags);
                uncheckMarker(mBlueFirstFlag);
                uncheckMarker(mBlueLastFlag);
                break;
        }
    }

    private void uncheckMarker(View view) {
        if (isAdded() && view != null) {
            if (!view.equals(mAbandonFlags)) {
                setMarkerLevel(mAbandonFlags, R.id.abandon_flags_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mRecallFlags)) {
                setMarkerLevel(mRecallFlags, R.id.recall_flags_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mPostponeFlags)) {
                setMarkerLevel(mPostponeFlags, R.id.postpone_flags_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mCourseFlags)) {
                setMarkerLevel(mCourseFlags, R.id.course_flags_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mBlueFirstFlag)) {
                setMarkerLevel(mBlueFirstFlag, R.id.first_blue_flags_marker, LEVEL_NORMAL);
            }

            if (!view.equals(mBlueLastFlag)) {
                setMarkerLevel(mBlueLastFlag, R.id.blue_down_flags_marker, LEVEL_NORMAL);
            }
        }
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        if (getRace() != null && getRaceState() != null) {
            switch (getRaceState().getStatus()) {
                case RUNNING:
                    TimePoint start = getRaceState().getStartTime();
                    if (start != null) {
                        long diff = now.minus(start.asMillis()).asMillis();
                        if (diff >= 60000) {
                            changeVisibility(mRecallLock, mRecallLayer, View.VISIBLE);
                        } else {
                            changeVisibility(mRecallLock, mRecallLayer, View.GONE);
                        }
                    } else {
                        changeVisibility(mRecallLock, mRecallLayer, View.GONE);
                    }
                    break;

                case FINISHING:
                    mBlueLastText.setText(TimeUtils.formatTimeAgo(getActivity(), now.minus(getRaceState().getFinishingTime().asMillis()).asMillis()));
                    changeVisibility(mRecallLock, mRecallLayer, View.VISIBLE);
                    break;

                case FINISHED:
                    changeVisibility(mRecallLock, mRecallLayer, View.VISIBLE);
                    break;

                default:
                    // nothing
            }
        }
    }

    private class RaceStateChangedListener extends BaseRaceStateChangedListener {
        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            super.onStatusChanged(state);

            checkStatus();
        }
    }

    private class AbandonFlagsClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = AbandonFlagsClick.class.getName();
        private final View container = mAbandonFlags;
        private final int markerId = R.id.abandon_flags_marker;

        public void onClick(View v) {
            if (mAbandonLock != null) {
                if (mAbandonLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_ABANDON);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(AbortFlagsFragment.newInstance(Flags.NOVEMBER, getString(R.string.flags_abandon)));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            disableToggle(container, markerId);
        }
    }

    private class RecallFlagsClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = RecallFlagsClick.class.getName();
        private final View container = mRecallFlags;
        private final int markerId = R.id.recall_flags_marker;

        public void onClick(View v) {
            if (mRecallLock != null) {
                if (mRecallLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_RECALL);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(RecallFlagsFragment.newInstance(getString(R.string.flags_recall)));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            disableToggle(container, markerId);
        }
    }

    private class PostponeFlagsClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = PostponeFlagsClick.class.getName();
        private final View container = mPostponeFlags;
        private final int markerId = R.id.postpone_flags_marker;

        public void onClick(View v) {
            if (mPostponeLock != null) {
                if (mPostponeLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_POSTPONE);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(AbortFlagsFragment.newInstance(Flags.AP, getString(R.string.flags_postpone)));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            disableToggle(container, markerId);
        }
    }

    private class CourseFlagsClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = CourseFlagsClick.class.getName();
        private final View container = mCourseFlags;
        private final int markerId = R.id.course_flags_marker;

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
                    replaceFragment(EmptyFragment.newInstance());
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            disableToggle(container, markerId);
        }
    }

    private class BlueFirstFlagClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = BlueFirstFlagClick.class.getName();
        private final View container = mBlueFirstFlag;
        private final int markerId = R.id.first_blue_flags_marker;

        @Override
        public void onClick(View v) {
            if (mBlueFirstLock != null) {
                if (mBlueFirstLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_BLUE_FIRST);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(MoreFlagsFragment.FinishTimeFragment.newInstance(0));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
            disableToggle(container, markerId);
        }
    }

    private class BlueLastFlagClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = BlueLastFlagClick.class.getName();
        private final View container = mBlueLastFlag;
        private final int markerId = R.id.blue_down_flags_marker;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mBlueLastLock != null) {
                if (mBlueLastLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
                    showChangeDialog(this);
                } else {
                    toggleFragment();
                }
            }
        }

        @Override
        public void onClick(View v) {
            toggleFragment();
        }

        private void toggleFragment() {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_BLUE_LAST);
            switch (toggleMarker(container, markerId)) {
                case LEVEL_NORMAL:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case LEVEL_TOGGLED:
                    replaceFragment(MoreFlagsFragment.FinishTimeFragment.newInstance(1));
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
                    if (AppConstants.INTENT_ACTION_TOGGLE_ABANDON.equals(data)) {
                        uncheckMarker(mAbandonFlags);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_RECALL.equals(data)) {
                        uncheckMarker(mRecallFlags);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_POSTPONE.equals(data)) {
                        uncheckMarker(mPostponeFlags);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_COURSE.equals(data)) {
                        uncheckMarker(mCourseFlags);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_BLUE_FIRST.equals(data)) {
                        uncheckMarker(mBlueFirstFlag);
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_BLUE_LAST.equals(data)) {
                        uncheckMarker(mBlueLastFlag);
                    } else {
                        uncheckMarker(new View(context));
                    }
                }
            }
        }
    }
}
