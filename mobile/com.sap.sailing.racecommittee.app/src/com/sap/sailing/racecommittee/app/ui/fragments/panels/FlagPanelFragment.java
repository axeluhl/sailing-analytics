package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.AbortFlagsFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.EmptyFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.MoreFlagsFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.RecallFlagsFragment;

public class FlagPanelFragment extends BasePanelFragment {

    private RaceStateChangedListener mStateListener;
    private IntentReceiver mReceiver;

    private View mAbandonFlags;
    private View mRecallFlags;
    private View mPostponeFlags;
    private View mCourseFlags;
    private View mMoreFlags;

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

        mAbandonFlags = layout.findViewById(R.id.abandon_flags);
        if (mAbandonFlags != null) {
            mAbandonFlags.setOnClickListener(new AbandonFlagsClick());
        }

        mRecallFlags = layout.findViewById(R.id.recall_flags);
        if (mRecallFlags != null) {
            mRecallFlags.setOnClickListener(new RecallFlagsClick());
        }

        mPostponeFlags = layout.findViewById(R.id.postpone_flags);
        if (mPostponeFlags != null) {
            mPostponeFlags.setOnClickListener(new PostponeFlagsClick());
        }

        mCourseFlags = layout.findViewById(R.id.course_flags);
        if (mCourseFlags != null) {
            mCourseFlags.setOnClickListener(new CourseFlagsClick());
        }

        mMoreFlags = layout.findViewById(R.id.more_flags);
        if (mMoreFlags != null) {
            mMoreFlags.setOnClickListener(new MoreFlagsClick());
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onStart();

        checkStatus();

        mStateListener = new RaceStateChangedListener();

        getRaceState().addChangedListener(mStateListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_TOGGLE);
        filter.addAction(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onStop();

        getRaceState().removeChangedListener(mStateListener);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private void checkStatus() {
        switch (getRace().getStatus()) {
            case UNSCHEDULED:
                changeVisibility(mPostponeFlags, View.VISIBLE);
                changeVisibility(mAbandonFlags, View.VISIBLE);
                changeVisibility(mRecallFlags, View.VISIBLE);
                changeVisibility(mCourseFlags, View.GONE);
                changeVisibility(mMoreFlags, View.VISIBLE);
                uncheckMarker(mCourseFlags);
                break;

            case SCHEDULED:
            case STARTPHASE:
                changeVisibility(mPostponeFlags, View.VISIBLE);
                changeVisibility(mAbandonFlags, View.GONE);
                changeVisibility(mRecallFlags, View.GONE);
                changeVisibility(mCourseFlags, View.GONE);
                changeVisibility(mMoreFlags, View.GONE);
                uncheckMarker(mAbandonFlags);
                uncheckMarker(mRecallFlags);
                uncheckMarker(mMoreFlags);
                break;

            case RUNNING:
                changeVisibility(mPostponeFlags, View.VISIBLE);
                changeVisibility(mAbandonFlags, View.VISIBLE);
                changeVisibility(mRecallFlags, View.VISIBLE);
                changeVisibility(mCourseFlags, View.GONE);
                changeVisibility(mMoreFlags, View.VISIBLE);
                uncheckMarker(mCourseFlags);
                break;

            case FINISHING:
                changeVisibility(mPostponeFlags, View.VISIBLE);
                changeVisibility(mAbandonFlags, View.VISIBLE);
                changeVisibility(mRecallFlags, View.GONE);
                changeVisibility(mCourseFlags, View.GONE);
                changeVisibility(mMoreFlags, View.GONE);
                uncheckMarker(mRecallFlags);
                uncheckMarker(mCourseFlags);
                uncheckMarker(mMoreFlags);
                break;

            case FINISHED:
                changeVisibility(mPostponeFlags, View.VISIBLE);
                changeVisibility(mAbandonFlags, View.VISIBLE);
                changeVisibility(mRecallFlags, View.VISIBLE);
                changeVisibility(mCourseFlags, View.GONE);
                changeVisibility(mMoreFlags, View.VISIBLE);
                uncheckMarker(mCourseFlags);
                break;

            default:
                changeVisibility(mPostponeFlags, View.GONE);
                changeVisibility(mAbandonFlags, View.GONE);
                changeVisibility(mRecallFlags, View.GONE);
                changeVisibility(mCourseFlags, View.GONE);
                changeVisibility(mMoreFlags, View.GONE);
                uncheckMarker(mPostponeFlags);
                uncheckMarker(mAbandonFlags);
                uncheckMarker(mRecallFlags);
                uncheckMarker(mCourseFlags);
                uncheckMarker(mMoreFlags);
                break;
        }
    }

    private void uncheckMarker(View view) {
        if (view != null) {
            if (!view.equals(mAbandonFlags)) {
                setMarkerLevel(mAbandonFlags, R.id.abandon_flags_marker, 0);
            }

            if (!view.equals(mRecallFlags)) {
                setMarkerLevel(mRecallFlags, R.id.recall_flags_marker, 0);
            }

            if (!view.equals(mPostponeFlags)) {
                setMarkerLevel(mPostponeFlags, R.id.postpone_flags_marker, 0);
            }

            if (!view.equals(mCourseFlags)) {
                setMarkerLevel(mCourseFlags, R.id.course_flags_marker, 0);
            }

            if (!view.equals(mMoreFlags)) {
                setMarkerLevel(mMoreFlags, R.id.more_flags_marker, 0);
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

    private class AbandonFlagsClick implements View.OnClickListener {

        private final String TAG = AbandonFlagsClick.class.getName();

        @Override
        public void onClick(View v) {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_ABANDON);
            switch (toggleMarker(v, R.id.abandon_flags_marker)) {
                case 0:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_RECALL);
            switch (toggleMarker(v, R.id.recall_flags_marker)) {
                case 0:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_POSTPONE);
            switch (toggleMarker(v, R.id.postpone_flags_marker)) {
                case 0:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_COURSE);
            switch (toggleMarker(v, R.id.course_flags_marker)) {
                case 0:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_MORE);
            switch (toggleMarker(v, R.id.more_flags_marker)) {
                case 0:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case 1:
                    replaceFragment(MoreFlagsFragment.FinishTimeFragment.newInstance(0));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
            }
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
                    } else if (AppConstants.INTENT_ACTION_TOGGLE_MORE.equals(data)) {
                        uncheckMarker(mMoreFlags);
                    } else {
                        uncheckMarker(new View(context));
                    }
                }
            }
        }
    }
}
