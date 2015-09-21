package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.adapters.AbortFlagsAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.AbortFlagsAdapter.AbortFlagItemClick;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceListFragment;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class AbortFlagsFragment extends RaceFragment implements AbortFlagItemClick {

    private final static String HEADER_TEXT = "headerText";

    public static AbortFlagsFragment newInstance(Flags flag, String headerText) {
        if (flag != Flags.AP && flag != Flags.NOVEMBER) {
            throw new IllegalArgumentException("The abort fragment can only be instantiated with AP or NOVEMBER, but was " + flag.name());
        }

        AbortFlagsFragment fragment = new AbortFlagsFragment();
        Bundle args = new Bundle();
        args.putString(AppConstants.FLAG_KEY, flag.name());
        args.putString(HEADER_TEXT, headerText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.flag_list, container, false);

        ListView listView = ViewHelper.get(layout, R.id.listView);
        if (listView != null) {
            Flags flag = Flags.valueOf(getArguments().getString(AppConstants.FLAG_KEY));
            listView.setAdapter(new AbortFlagsAdapter(getActivity(), this, flag));
        }

        TextView headerText = ViewHelper.get(layout, R.id.header_text);
        if (headerText != null) {
            headerText.setText(getArguments().getString(HEADER_TEXT, getString(R.string.not_available)));
        }

        View headerLayout = ViewHelper.get(layout, R.id.header_layout);
        if (headerLayout != null) {
            headerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendIntent(AppConstants.INTENT_ACTION_CLEAR_TOGGLE);
                    sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                }
            });
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
    }

    private void logFlag(Flags flag) {
        switch (flag) {
        case ALPHA:
            ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_ALPHA, getRace().getId());
            break;

        case HOTEL:
            ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_HOTEL, getRace().getId());
            break;

        default:
            ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_NONE, getRace().getId());
            break;
        }
    }

    private void setProtestTime() {
        RaceListFragment.showProtest(getActivity(), getRace());
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
