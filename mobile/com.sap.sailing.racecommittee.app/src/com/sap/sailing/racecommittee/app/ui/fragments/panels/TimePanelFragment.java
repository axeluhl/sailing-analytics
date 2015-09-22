package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import static com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult.ResolutionFailed.NO_START_TIME_SET;

import java.text.SimpleDateFormat;

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
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.StartTimeFragment;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TimePanelFragment extends BasePanelFragment {

    public final static String TOGGLED = "toggled";

    private RaceStateChangedListener mStateListener;
    private IntentReceiver mReceiver;
    private SimpleDateFormat dateFormat;

    private View mRaceHeader;
    private View mTimeLock;
    private TextView mCurrentTime;
    private TextView mHeaderTime;
    private TextView mTimeStart;
    private TextView mTimeFinish;
    private ImageView mLinkIcon;
    private Boolean mLinkedRace = null;

    public TimePanelFragment() {
        mReceiver = new IntentReceiver();
    }

    public static TimePanelFragment newInstance(Bundle args) {
        TimePanelFragment fragment = new TimePanelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_panel_time, container, false);

        dateFormat = new SimpleDateFormat("HH:mm:ss", getResources().getConfiguration().locale);
        mStateListener = new RaceStateChangedListener();

        mRaceHeader = ViewHelper.get(layout, R.id.race_content_header);
        if (mRaceHeader != null) {
            mRaceHeader.setOnClickListener(new RaceHeaderClick());
        }

        mTimeLock = ViewHelper.get(layout, R.id.time_start_lock);
        mCurrentTime = ViewHelper.get(layout, R.id.current_time);
        mTimeFinish = ViewHelper.get(layout, R.id.time_finish);
        mHeaderTime = ViewHelper.get(layout, R.id.timer_text);
        mTimeStart = ViewHelper.get(layout, R.id.time_start);
        mLinkIcon = ViewHelper.get(layout, R.id.linked_race);

        if (getArguments().getBoolean(TOGGLED, false)) {
            toggleMarker(layout, R.id.time_marker);
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        notifyTick(MillisecondsTimePoint.now());
        checkStatus();

        getRaceState().addChangedListener(mStateListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.INTENT_ACTION_TOGGLE);
        filter.addAction(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
        filter.addAction(AppConstants.INTENT_ACTION_TIME_SHOW);
        filter.addAction(AppConstants.INTENT_ACTION_TIME_HIDE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);

        sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        getRaceState().removeChangedListener(mStateListener);

        TickSingleton.INSTANCE.unregisterListener(this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        if (mCurrentTime != null) {
            mCurrentTime.setText(dateFormat.format(now.asMillis()));
            mCurrentTime.setVisibility(View.VISIBLE);
        }

        if (getRace() != null && getRace().getState() != null) {
            TimePoint startTime = getRace().getState().getStartTime();

            if (mTimeStart != null) {
                if (startTime != null) {
                    mTimeStart.setText(getString(R.string.time_start, dateFormat.format(startTime.asDate())));
                } else {
                    mTimeStart.setText(getString(R.string.time_start, "N/A"));
                }
            }

            if (mTimeFinish != null) {
                mTimeFinish.setVisibility(View.VISIBLE);
            }

            if (mHeaderTime != null && startTime != null) {
                String time;
                int resId;
                if (startTime.after(now)) {
                    resId = R.string.race_start_time_in;
                    time = TimeUtils.formatDurationUntil(startTime.minus(now.asMillis()).asMillis());
                } else {
                    resId = R.string.race_start_time_ago;
                    time = TimeUtils.formatDurationSince(now.minus(startTime.asMillis()).asMillis());
                }
                mHeaderTime.setText(getString(resId, time));
            }
        }

        if (mLinkedRace == null) {
            StartTimeFinderResult result = getRaceState().getStartTimeFinderResult();
            if (result != null) {
                mLinkedRace = result.isDependentStartTime();
                if (mLinkedRace && mHeaderTime != null && result.getResolutionFailed() == NO_START_TIME_SET) {
                    SimpleRaceLogIdentifier identifier = Util.get(result.getRacesDependingOn(), 0);
                    ManagedRace race = DataManager.create(getActivity()).getDataStore().getRace(identifier);
                    mHeaderTime
                        .setText(getString(R.string.minutes_after_long, result.getStartTimeDiff().asMinutes(), RaceHelper.getRaceName(race, " / ")));
                }
            }
        }

        if (mLinkedRace != null && mLinkIcon != null) {
            mLinkIcon.setVisibility(mLinkedRace ? View.VISIBLE : View.GONE);
        }
    }

    private void uncheckMarker(View view) {
        if (view != null) {
            if (!view.equals(mRaceHeader)) {
                resetFragment(mTimeLock, getFrameId(getActivity(), R.id.race_edit, R.id.race_content), StartTimeFragment.class);
                setMarkerLevel(mRaceHeader, R.id.time_marker, 0);
            }
        }
    }

    private void checkStatus() {
        switch (getRace().getStatus()) {
            case UNSCHEDULED:
                changeVisibility(mTimeLock, View.GONE);
                break;

            case PRESCHEDULED:
                changeVisibility(mTimeLock, View.GONE);
                break;

            case SCHEDULED:
                changeVisibility(mTimeLock, View.GONE);
                break;

            case STARTPHASE:
                changeVisibility(mTimeLock, View.GONE);
                break;

            case RUNNING:
                changeVisibility(mTimeLock, View.VISIBLE);
                break;

            case FINISHING:
                changeVisibility(mTimeLock, View.VISIBLE);
                break;

            case FINISHED:
                changeVisibility(mTimeLock, View.VISIBLE);
                break;

            default:
                changeVisibility(mTimeLock, View.VISIBLE);
                break;
        }
    }

    private class RaceHeaderClick implements View.OnClickListener, DialogInterface.OnClickListener {

        private final String TAG = RaceHeaderClick.class.getName();
        private final View container = mRaceHeader;
        private final int markerId = R.id.time_marker;

        public void onClick(View v) {
            if (mTimeLock != null) {
                if (mTimeLock.getVisibility() == View.VISIBLE && isNormal(container, markerId)) {
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_TIME);
            switch (toggleMarker(container, markerId)) {
                case 0:
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                    break;

                case 1:
                    replaceFragment(StartTimeFragment.newInstance(StartTimeFragment.MODE_TIME_PANEL));
                    break;

                default:
                    ExLog.i(getActivity(), TAG, "Unknown return value");
                    break;
            }
            disableToggle(container, markerId);
        }
    }

    private class IntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            View view = new View(context);
            String action = intent.getAction();
            if (AppConstants.INTENT_ACTION_CLEAR_TOGGLE.equals(action)) {
                uncheckMarker(view);
            }
            if (AppConstants.INTENT_ACTION_TOGGLE.equals(action)) {
                if (intent.getExtras() != null) {
                    String data = intent.getExtras().getString(AppConstants.INTENT_ACTION_EXTRA);
                    if (AppConstants.INTENT_ACTION_TOGGLE_TIME.equals(data)) {
                        uncheckMarker(mRaceHeader);
                    } else {
                        uncheckMarker(view);
                    }
                }
            }

            view = getActivity().findViewById(R.id.race_panel_time);
            if (getActivity().findViewById(R.id.race_edit) == null && view != null) {
                if (AppConstants.INTENT_ACTION_TIME_HIDE.equals(action)) {
                    view.setVisibility(View.GONE);
                }

                if (AppConstants.INTENT_ACTION_TIME_SHOW.equals(action)) {
                    view.setVisibility(View.VISIBLE);
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
        public void onStatusChanged(ReadonlyRaceState state) {
            super.onStatusChanged(state);

            checkStatus();
            uncheckMarker(mView);
        }

        @Override
        public void onStartTimeChanged(ReadonlyRaceState state) {
            super.onStartTimeChanged(state);

            mLinkedRace = null;
        }
    }
}
