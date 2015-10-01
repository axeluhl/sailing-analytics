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
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItemAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.StartModeItem;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LineStartModeFragment extends BaseFragment {

    private ListView mListView;
    private RRS26RacingProcedure mProcedure;
    private int mFlagSize;

    public static LineStartModeFragment newInstance(@START_MODE_VALUES int startMode) {
        LineStartModeFragment fragment = new LineStartModeFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            switch (getArguments().getInt(START_MODE, 0)) {
                case 1:
                    if (getView() != null) {
                        View header = getView().findViewById(R.id.header);
                        header.setVisibility(View.GONE);
                    }
                    break;

                default:
                    break;
            }
        }

        mProcedure = getRaceState().getTypedRacingProcedure();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_mode, container, false);

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

        mFlagSize = getResources().getInteger(R.integer.flag_size_large);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        ArrayList<StartModeItem> startModes = new ArrayList<>();
        List<Flags> flags = mProcedure.getConfiguration().getStartModeFlags();
        int position = 0;
        int selected = -1;
        for (Flags flag : flags) {
            StartModeItem startMode = new StartModeItem(flag);
            startMode.setImage(FlagsResources.getFlagDrawable(getActivity(), startMode.getFlagName(), mFlagSize));
            startModes.add(startMode);
        }
        Collections.sort(startModes, new StartModeComparator());
        for(StartModeItem startModeItem: startModes){
            if(startModeItem.getFlag().equals(mProcedure.getStartModeFlag())){
                selected = position;
            }
            position++;
        }
        final CheckedItemAdapter adapter = new CheckedItemAdapter(getActivity(), startModes);
        adapter.setCheckedPosition(selected);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setCheckedPosition(position);
                StartModeItem item = (StartModeItem) adapter.getItem(position);
                if (item != null) {
                    onClick(item);
                }
            }
        });

        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

    @Override
    public void onPause() {
        super.onPause();

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    public void onClick(StartModeItem startMode) {
        boolean sameFlag = false;
        if (startMode.getFlag() == mProcedure.getStartModeFlag()) {
            sameFlag = true;
        }
        mProcedure.setStartModeFlag(MillisecondsTimePoint.now(), startMode.getFlag());
        if (getArguments() != null && getArguments().getInt(START_MODE, START_MODE_PRESETUP) == START_MODE_PRESETUP) {
            openMainScheduleFragment();
        } else {
            if (sameFlag) {
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
            }
        }
    }

    private class StartModeComparator implements Comparator<StartModeItem> {

        @Override
        public int compare(StartModeItem left, StartModeItem right) {
            return left.getFlagName().compareToIgnoreCase(right.getFlagName());
        }
    }
}
