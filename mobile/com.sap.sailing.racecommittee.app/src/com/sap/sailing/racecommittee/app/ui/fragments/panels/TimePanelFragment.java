package com.sap.sailing.racecommittee.app.ui.fragments.panels;

import static com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult.ResolutionFailed.NO_START_TIME_SET;

import java.text.SimpleDateFormat;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.BroadcastManager;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.PenaltyFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.StartTimeFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.TrackingListFragment;
import com.sap.sailing.racecommittee.app.ui.layouts.TimePanelHeaderLayout;
import com.sap.sailing.racecommittee.app.ui.views.PanelButton;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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

public class TimePanelFragment extends BasePanelFragment {

    private final static String TOGGLED = "toggled";

    private RaceStateChangedListener mStateListener;
    private IntentReceiver mReceiver;
    private SimpleDateFormat dateFormat;

    private TimePanelHeaderLayout mRaceHeader;
    private PanelButton mCompetitorList;

    private View mTimeLock;
    private TextView mCurrentTime;
    private TextView mHeaderTime;
    private TextView mTimeStart;
    private TextView mFirstVesselDuration;
    private ImageView mLinkIcon;
    private Boolean mLinkedRace = null;

    private CompetitorPanelClick mClickListener;
    private TimePoint mLastFinishingTime = null;

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
        mRaceHeader.setRunnable(new Runnable() {
            @Override
            public void run() {
                new RaceHeaderClick().onClick(null);
            }
        });

        mCompetitorList = ViewHelper.get(layout, R.id.button_competitor);

        mTimeLock = ViewHelper.get(layout, R.id.time_start_lock);
        mCurrentTime = ViewHelper.get(layout, R.id.current_time);
        mHeaderTime = ViewHelper.get(layout, R.id.timer_text);
        mTimeStart = ViewHelper.get(layout, R.id.time_start);
        mLinkIcon = ViewHelper.get(layout, R.id.linked_race);
        mFirstVesselDuration = ViewHelper.get(layout, R.id.first_vessel_duration);

        if (getArguments().getBoolean(TOGGLED, false)) {
            toggleMarker(layout, R.id.time_marker);
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (preferences.getRacingProcedureIsResultEntryEnabled(getRaceState().getRacingProcedure().getType())) {
            mClickListener = new CompetitorPanelClick();
            mCompetitorList.setListener(mClickListener);
            mCompetitorList.setVisibility(View.VISIBLE);
            checkWarnings(getRaceState());
        }
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
        filter.addAction(AppConstants.INTENT_ACTION_ON_LIFECYCLE);
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
                    SimpleRaceLogIdentifier identifier = Util.get(result.getDependingOnRaces(), 0);
                    ManagedRace race = DataManager.create(getActivity()).getDataStore().getRace(identifier);
                    mHeaderTime.setText(getString(R.string.minutes_after_long, result.getStartTimeDiff().asMinutes(),
                            RaceHelper.getShortReverseRaceName(race, " / ", getRace())));
                }
            }
        }

        if (mLinkedRace != null && mLinkIcon != null) {
            mLinkIcon.setVisibility(mLinkedRace ? View.VISIBLE : View.GONE);
        }

        if (mFirstVesselDuration != null && mFirstVesselDuration.getVisibility() == View.VISIBLE) {
            if (!getRaceState().getFinishingTime().equals(mLastFinishingTime)) {
                mLastFinishingTime = getRaceState().getFinishingTime();
                String raceDuration = TimeUtils.formatTimeAgo(getActivity(),
                        mLastFinishingTime.minus(getRaceState().getStartTime().asMillis()).asMillis());
                mFirstVesselDuration.setText(raceDuration);
            }
        }
    }

    private void uncheckMarker(View view) {
        if (isAdded()) {
            if (!mRaceHeader.equals(view) && !isNormal(mRaceHeader, R.id.time_marker)) {
                resetFragment(mTimeLock, getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false),
                        StartTimeFragment.class);
                setMarkerLevel(mRaceHeader, R.id.time_marker, LEVEL_NORMAL);
            }
            if (!mCompetitorList.equals(view)) {
                resetFragment(mCompetitorList.isLocked(),
                        getFrameId(getActivity(), R.id.race_edit, R.id.race_content, false), PenaltyFragment.class);
                mCompetitorList.setMarkerLevel(PanelButton.LEVEL_NORMAL);
            }
        }
    }

    private void checkStatus() {
        switch (getRace().getStatus()) {
        case UNSCHEDULED:
            changeVisibility(mTimeLock, null, View.GONE);
            changeVisibility(mFirstVesselDuration, null, View.GONE);
            break;

        case PRESCHEDULED:
            changeVisibility(mTimeLock, null, View.GONE);
            changeVisibility(mFirstVesselDuration, null, View.GONE);
            break;

        case SCHEDULED:
            changeVisibility(mTimeLock, null, View.GONE);
            changeVisibility(mFirstVesselDuration, null, View.GONE);
            break;

        case STARTPHASE:
            changeVisibility(mTimeLock, null, View.GONE);
            changeVisibility(mFirstVesselDuration, null, View.GONE);
            break;

        case RUNNING:
            changeVisibility(mTimeLock, null, View.VISIBLE);
            changeVisibility(mFirstVesselDuration, null, View.GONE);
            break;

        case FINISHING:
            changeVisibility(mTimeLock, null, View.VISIBLE);
            changeVisibility(mFirstVesselDuration, null, View.VISIBLE);
            break;

        case FINISHED:
            changeVisibility(mTimeLock, null, View.VISIBLE);
            changeVisibility(mFirstVesselDuration, null, View.VISIBLE);
            break;

        default:
            changeVisibility(mTimeLock, null, View.VISIBLE);
            changeVisibility(mFirstVesselDuration, null, View.GONE);
            break;
        }
    }

    private void checkWarnings(ReadonlyRaceState state) {
        CompetitorResults draft = state.getFinishPositioningList();
        CompetitorResults confirmed = state.getConfirmedFinishPositioningList();
        mCompetitorList.showAdditionalImage(
                (draft != null && draft.hasConflicts()) || (confirmed != null && confirmed.hasConflicts()));
    }

    private class RaceStateChangedListener extends BaseRaceStateChangedListener {

        @Override
        public void onStatusChanged(ReadonlyRaceState state) {
            super.onStatusChanged(state);

            checkStatus();
            uncheckMarker(null);
        }

        @Override
        public void onStartTimeChanged(ReadonlyRaceState state) {
            super.onStartTimeChanged(state);

            mLinkedRace = null;
        }

        @Override
        public void onFinishingPositioningsChanged(ReadonlyRaceState state) {
            super.onFinishingPositioningsChanged(state);

            checkWarnings(state);
        }

        @Override
        public void onFinishingPositionsConfirmed(ReadonlyRaceState state) {
            super.onFinishingPositionsConfirmed(state);

            checkWarnings(state);
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
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                    AppConstants.INTENT_ACTION_TOGGLE_TIME);
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

    private class CompetitorPanelClick implements PanelButton.PanelButtonClick {

        private final String TAG = CompetitorPanelClick.class.getName();

        @Override
        public void onClick(PanelButton view) {
            sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA,
                    AppConstants.INTENT_ACTION_TOGGLE_COMPETITOR);
            switch (view.toggleMarker()) {
            case PanelButton.LEVEL_NORMAL:
                Intent intent = new Intent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                intent.putExtra(AppConstants.INTENT_ACTION_EXTRA_FORCED, true);
                BroadcastManager.getInstance(getActivity()).addIntent(intent);
                break;

            case PanelButton.LEVEL_TOGGLED:
                Bundle args = new Bundle();
                RaceFragment content;
                args.putSerializable(AppConstants.INTENT_EXTRA_RACE_ID, getRace().getId());
                if (getRace().getStatus() != RaceLogRaceStatus.FINISHING) {
                    content = PenaltyFragment.newInstance();
                } else {
                    content = TrackingListFragment.newInstance(args, 1);
                }
                replaceFragment(content, R.id.race_content);
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

    private class IntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isAdded()) {
                String action = intent.getAction();
                if (AppConstants.INTENT_ACTION_CLEAR_TOGGLE.equals(action)) {
                    uncheckMarker(null);
                }

                if (AppConstants.INTENT_ACTION_TOGGLE.equals(action)) {
                    if (intent.getExtras() != null) {
                        String data = intent.getStringExtra(AppConstants.INTENT_ACTION_EXTRA);
                        switch (data) {
                        case AppConstants.INTENT_ACTION_TOGGLE_TIME:
                            uncheckMarker(mRaceHeader);
                            break;
                        case AppConstants.INTENT_ACTION_TOGGLE_COMPETITOR:
                            uncheckMarker(mCompetitorList);
                            break;
                        default:
                            uncheckMarker(null);
                            break;
                        }
                    }
                }

                if (AppConstants.INTENT_ACTION_ON_LIFECYCLE.equals(action)) {
                    if (intent.getExtras() != null) {
                        String event = intent.getStringExtra(AppConstants.INTENT_ACTION_EXTRA_LIFECYCLE);
                        String data = intent.getStringExtra(AppConstants.INTENT_ACTION_EXTRA);
                        if (AppConstants.INTENT_ACTION_EXTRA_START.equals(event)) {
                            if (AppConstants.INTENT_ACTION_TOGGLE_COMPETITOR.equals(data)) {
                                mCompetitorList.setMarkerLevel(PanelButton.LEVEL_TOGGLED);
                            }
                        }
                        if (AppConstants.INTENT_ACTION_EXTRA_STOP.equals(event)) {
                            if (AppConstants.INTENT_ACTION_TOGGLE_COMPETITOR.equals(data)) {
                                mCompetitorList.setMarkerLevel(PanelButton.LEVEL_NORMAL);
                            }
                        }
                    }
                }

                View view = getActivity().findViewById(R.id.race_panel_time);
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
    }
}
