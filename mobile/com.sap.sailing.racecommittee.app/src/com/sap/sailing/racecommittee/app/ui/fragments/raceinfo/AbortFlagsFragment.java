package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.adapters.AbortFlagsAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.AbortFlagsAdapter.AbortFlagItemClick;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.ProtestTimeDialogFragment;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class AbortFlagsFragment extends RaceFragment implements AbortFlagItemClick {

    public static AbortFlagsFragment newInstance(Flags flag) {
        if (flag != Flags.AP && flag != Flags.NOVEMBER) {
            throw new IllegalArgumentException("The abort fragment can only be instantiated with AP or NOVEMBER, but was " + flag.name());
        }

        AbortFlagsFragment fragment = new AbortFlagsFragment();
        Bundle args = new Bundle();
        args.putString(AppConstants.FLAG_KEY, flag.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.flag_list, container, false);

        ListView listView = (ListView) layout.findViewById(R.id.listView);
        if (listView != null) {
            Flags flag = Flags.valueOf(getArguments().getString(AppConstants.FLAG_KEY));
            listView.setAdapter(new AbortFlagsAdapter(getActivity(), this, flag));
        }

        return layout;
    }

    @Override
    public void onClick(Flags flag) {
        TimePoint now = MillisecondsTimePoint.now();
        RaceState state = getRaceState();
        Flags mainFlag = Flags.valueOf(getArguments().getString(AppConstants.FLAG_KEY));
        switch (mainFlag) {
        case AP:
            logFlag(flag);
            state.setAborted(now, /* postponed */ true, flag);
            if (flag != Flags.NONE) {
                setProtestTime();
            }
            break;

        case NOVEMBER:
            logFlag(flag);
            state.setAborted(now, /* postponed */ false, flag);
            if (flag != Flags.NONE) {
                setProtestTime();
            }
            break;

        default:
            logFlag(flag);
            break;
        }
        state.setAdvancePass(now);

        RacingActivity activity = (RacingActivity) getActivity();
        activity.onRaceItemClicked(getRace());
    }

    private void logFlag(Flags flag) {
        switch (flag) {
        case ALPHA:
            ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_ALPHA, getRace().getId().toString());
            break;

        case HOTEL:
            ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_HOTEL, getRace().getId().toString());
            break;

        default:
            ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_NONE, getRace().getId().toString());
            break;
        }
    }

    private void setProtestTime() {
        ProtestTimeDialogFragment fragment = ProtestTimeDialogFragment.newInstance(getRace());
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onResume() {
        super.onResume();

        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

    @Override
    public void onPause() {
        super.onPause();

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }
}
