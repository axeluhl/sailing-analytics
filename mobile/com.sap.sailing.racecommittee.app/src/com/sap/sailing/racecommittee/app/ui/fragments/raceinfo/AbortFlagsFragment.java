package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.adapters.AbortFlagsAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.AbortFlagsAdapter.AbortFlagItemClick;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class AbortFlagsFragment extends RaceFragment implements AbortFlagItemClick {

    private static final String FLAG = "flag";

    public static AbortFlagsFragment newInstance(Flags flag) {
        AbortFlagsFragment fragment = new AbortFlagsFragment();
        Bundle args = new Bundle();
        args.putSerializable(FLAG, flag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.flag_list, container, false);

        ListView listView = (ListView) layout.findViewById(R.id.listView);
        if (listView != null) {
            Flags flag = (Flags) getArguments().getSerializable(FLAG);
            listView.setAdapter(new AbortFlagsAdapter(getActivity(), this, flag));
        }

        return layout;
    }

    @Override
    public void onClick(Flags flag) {
        TimePoint now = MillisecondsTimePoint.now();
        RaceState state = getRaceState();
        switch (flag) {
            case AP:
                ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_ALPHA, getRace().getId().toString());
                state.setAborted(now, /* postponed */ true, flag);
                break;

            case NOVEMBER:
                ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_HOTEL, getRace().getId().toString());
                state.setAborted(now, /* postponed */ false, flag);
                break;

            default:
                ExLog.i(getActivity(), LogEvent.RACE_CHOOSE_ABORT_NONE, getRace().getId().toString());
                break;
        }
        state.setAdvancePass(now);
    }
}
