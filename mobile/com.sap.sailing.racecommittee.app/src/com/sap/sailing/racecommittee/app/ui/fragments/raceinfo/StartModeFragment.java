package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

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
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CheckedItemListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.coursedesign.CheckedListItem;
import com.sap.sailing.racecommittee.app.ui.adapters.unscheduled.StartMode;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StartModeFragment extends BaseFragment{

    private final static String START_MODE = "startMode";
    private ListView mListView;
    private RRS26RacingProcedure mProcedure;

    public static StartModeFragment newInstance(int startMode) {
        StartModeFragment fragment = new StartModeFragment();
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

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();

        ArrayList<StartMode> startModes = new ArrayList<>();
        List<Flags> flags = mProcedure.getConfiguration().getStartModeFlags();
        int position = 0;
        int selected = -1;
        for (Flags flag : flags) {
            if (mProcedure.getStartModeFlag() != null) {
                selected = position;
            }
            StartMode startMode = new StartMode(flag);
            startMode.setImage(FlagsResources.getFlagDrawable(getActivity(), startMode.getFlagName(), 64));
            startModes.add(startMode);
            position++;
        }

        Collections.sort(startModes, new StartModeComparator());
        final CheckedItemListAdapter adapter = new CheckedItemListAdapter(getActivity(), startModes);
        adapter.setCheckedPostion(selected);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedListItem item = adapter.getItem(position);
                if(item != null && item instanceof StartMode) {
                    onClick((StartMode) item);
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

    public void onClick(StartMode startMode) {
        boolean sameFlag = false;
        if (startMode.getFlag() == mProcedure.getStartModeFlag()) {
            sameFlag = true;
        }
        mProcedure.setStartModeFlag(MillisecondsTimePoint.now(), startMode.getFlag());
        if (getArguments() != null && getArguments().getInt(START_MODE, 0) == 0) {
            openMainScheduleFragment();
        } else {
            if (sameFlag) {
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
            }
        }
    }

    private class StartModeComparator implements Comparator<StartMode> {

        @Override
        public int compare(StartMode left, StartMode right) {
            return left.getFlagName().compareToIgnoreCase(right.getFlagName());
        }
    }
}
