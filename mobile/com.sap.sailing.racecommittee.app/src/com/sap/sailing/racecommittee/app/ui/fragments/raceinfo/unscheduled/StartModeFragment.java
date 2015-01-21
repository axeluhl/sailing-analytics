package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.unscheduled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.unscheduled.StartMode;
import com.sap.sailing.racecommittee.app.ui.adapters.unscheduled.StartModeAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.NextFragmentListener;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StartModeFragment extends RaceFragment {

    private class StartModeComparator implements Comparator<StartMode> {

        @Override
        public int compare(StartMode left, StartMode right) {
            StartMode leftStartMode = (StartMode) left;
            StartMode rightStartMode = (StartMode) right;
            return leftStartMode.getFlagName().compareToIgnoreCase(rightStartMode.getFlagName());
        }
    }

    private StartModeAdapter mAdapter;
    private NextFragmentListener mListener;
    private ListView mListView;
    private RRS26RacingProcedure mProcedure;

    public StartModeFragment() {

    }

    public StartModeFragment(NextFragmentListener listener) {
        mListener = listener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mProcedure = getRaceState().getTypedRacingProcedure();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_schedule_mode, container, false);

        mListView = (ListView) view.findViewById(R.id.listMode);

        Button confirm = (Button) view.findViewById(R.id.confirm);
        if (confirm != null) {
            confirm.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    StartMode checkedItem = null;
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        StartMode item = mAdapter.getItem(i);
                        if (item.isChecked()) {
                            checkedItem = item;
                        }
                    }
                    if (checkedItem != null) {
                        mProcedure.setStartModeFlag(MillisecondsTimePoint.now(), checkedItem.getFlag());
                        mListener.nextFragment();
                    } else {
                        Toast.makeText(getActivity(), "Please choose one start mode", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ArrayList<StartMode> startMode = new ArrayList<StartMode>();

        List<Flags> flags = mProcedure.getConfiguration().getStartModeFlags();

        for (Flags flag : flags) {
            if (mProcedure.getStartModeFlag() == null) {
                startMode.add(new StartMode(flag));
            } else {
                startMode.add(new StartMode(flag, mProcedure.getStartModeFlag() == flag));
            }
        }

        Collections.sort(startMode, new StartModeComparator());
        mAdapter = new StartModeAdapter(getActivity(), startMode);
        mListView.setAdapter(mAdapter);
    }
}
