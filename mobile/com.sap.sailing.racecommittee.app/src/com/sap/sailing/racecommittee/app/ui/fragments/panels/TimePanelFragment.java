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
import android.widget.TextView;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.StartTimeFragment;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.text.SimpleDateFormat;

public class TimePanelFragment extends BasePanelFragment implements TickListener {

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

        mRaceHeader = ViewHolder.get(layout, R.id.race_content_header);
        if (mRaceHeader != null) {
            mRaceHeader.setOnClickListener(new RaceHeaderClick());
        }

        mTimeLock = ViewHolder.get(layout, R.id.time_start_lock);
        mCurrentTime = ViewHolder.get(layout, R.id.current_time);
        mTimeFinish = ViewHolder.get(layout, R.id.time_finish);
        mHeaderTime = ViewHolder.get(layout, R.id.timer_text);
        mTimeStart = ViewHolder.get(layout, R.id.time_start);
        if (getArguments().getBoolean(TOGGLED, false)) {
            toggleMarker(layout, R.id.time_marker);
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onStart();

        TickSingleton.INSTANCE.registerListener(this);
        notifyTick();
        checkStatus();

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

        TickSingleton.INSTANCE.unregisterListener(this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void notifyTick() {
        super.notifyTick();

        TimePoint now = MillisecondsTimePoint.now();

        if (mCurrentTime != null) {
            mCurrentTime.setText(dateFormat.format(now.asMillis()));
            mCurrentTime.setVisibility(View.VISIBLE);
        }

        if (getRace() != null && getRace().getState() != null) {
            TimePoint startTime = getRace().getState().getStartTime();

            if (mTimeStart != null && startTime != null) {
                mTimeStart.setText(getString(R.string.time_start).replace("#TIME#", dateFormat.format(startTime.asDate())));
            }

            if (mTimeFinish != null) {
                mTimeFinish.setVisibility(View.VISIBLE);
            }

            if (mHeaderTime != null && startTime != null) {
                String time;
                if (startTime.asMillis() > now.asMillis()) {
                    time = TimeUtils.formatDurationUntil(startTime.minus(now.asMillis()).asMillis());
                } else {
                    time = TimeUtils.formatDurationSince(now.minus(startTime.asMillis()).asMillis());
                }
                mHeaderTime.setText(getString(R.string.time).replace("#TIME#", time));
            }
        }
    }

    private void uncheckMarker(View view) {
        if (view != null) {
            if (!view.equals(mRaceHeader)) {
                resetFragment(mTimeLock, this.getClass());
                setMarkerLevel(mRaceHeader, R.id.time_marker, 0);
            }
        }
    }

    private void checkStatus() {
        switch (getRace().getStatus()) {
            case UNSCHEDULED:
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

    private class RaceHeaderClick implements View.OnClickListener {

        private final String TAG = RaceHeaderClick.class.getName();

        public void onClick(View v) {
            if (mTimeLock == null || mTimeLock.getVisibility() == View.GONE) {
                sendIntent(AppConstants.INTENT_ACTION_TOGGLE, AppConstants.INTENT_ACTION_EXTRA, AppConstants.INTENT_ACTION_TOGGLE_TIME);
                switch (toggleMarker(v, R.id.time_marker)) {
                    case 0:
                        sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
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
    }
}
