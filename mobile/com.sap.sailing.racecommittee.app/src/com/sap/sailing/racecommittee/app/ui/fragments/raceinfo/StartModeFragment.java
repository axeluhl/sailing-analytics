package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.unscheduled.StartMode;
import com.sap.sailing.racecommittee.app.ui.adapters.unscheduled.StartModeAdapter;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StartModeFragment extends ScheduleFragment implements StartModeAdapter.StartModeClick {

    private final static String STARTMODE = "startMode";

    private class StartModeComparator implements Comparator<StartMode> {

        @Override
        public int compare(StartMode left, StartMode right) {
            return left.getFlagName().compareToIgnoreCase(right.getFlagName());
        }
    }

    private StartModeAdapter mAdapter;
    private ListView mListView;
    private RRS26RacingProcedure mProcedure;

    public StartModeFragment() {

    }

    public static StartModeFragment newInstance(int startMode) {
        StartModeFragment fragment = new StartModeFragment();
        Bundle args = new Bundle();
        args.putInt(STARTMODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mProcedure = getRaceState().getTypedRacingProcedure();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_mode, container, false);

        if (getArguments() != null) {
            switch (getArguments().getInt(STARTMODE, 0)) {
                case 1:
                    layout.findViewById(R.id.race_header).setVisibility(View.VISIBLE);
                    View header = layout.findViewById(R.id.header);
                    header.setVisibility(View.GONE);
                    break;

                default:
                    break;
            }
        }

        mListView = (ListView) layout.findViewById(R.id.listView);

        LinearLayout headerText = (LinearLayout) layout.findViewById(R.id.header_text);
        if (headerText != null) {
            headerText.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    openMainScheduleFragment();
                }
            });
        }

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        ArrayList<StartMode> startMode = new ArrayList<>();
        List<Flags> flags = mProcedure.getConfiguration().getStartModeFlags();

        for (Flags flag : flags) {
            if (mProcedure.getStartModeFlag() == null) {
                startMode.add(new StartMode(flag));
            } else {
                startMode.add(new StartMode(flag, mProcedure.getStartModeFlag() == flag));
            }
        }

        Collections.sort(startMode, new StartModeComparator());
        mAdapter = new StartModeAdapter(getActivity(), startMode, this);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(StartMode startMode) {
        mProcedure.setStartModeFlag(MillisecondsTimePoint.now(), startMode.getFlag());
        if (getArguments() != null && getArguments().getInt(STARTMODE, 0) == 0) {
            openMainScheduleFragment();
        } else {
            replaceFragment(RaceFlagViewerFragment.newInstance(), R.id.race_frame);
            sendIntent(R.string.intent_uncheck_all);
        }
    }
}
