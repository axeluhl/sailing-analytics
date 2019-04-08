package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItemAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.StartModeItem;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class StartModeFragment extends BaseFragment {

    private ListView mListView;
    private ConfigurableStartModeFlagRacingProcedure mLineStartProcedure;
    private int mFlagSize;

    public static StartModeFragment newInstance(@START_MODE_VALUES int startMode) {
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
            switch (getArguments().getInt(START_MODE, START_MODE_PRESETUP)) {
            case START_MODE_PLANNED:
                if (AppUtils.with(getActivity()).isLandscape()) {
                    if (getView() != null) {
                        View header = getView().findViewById(R.id.header);
                        header.setVisibility(View.GONE);
                    }
                }
                break;

            default:
                break;
            }
        }

        mLineStartProcedure = getRaceState().getTypedRacingProcedure();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_mode, container, false);

        mListView = (ListView) layout.findViewById(R.id.listView);

        HeaderLayout header = (HeaderLayout) layout.findViewById(R.id.header);
        if (header != null) {
            header.setHeaderOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    goHome();
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
        List<Flags> flags = mLineStartProcedure.getConfiguration().getStartModeFlags();
        if (flags != null) {
            int position = 0;
            int selected = -1;
            for (Flags flag : flags) {
                StartModeItem startMode = new StartModeItem(flag);
                startMode.setImage(FlagsResources.getFlagDrawable(getActivity(), startMode.getFlagName(), mFlagSize));
                startModes.add(startMode);
            }
            Collections.sort(startModes, new StartModeComparator());
            for (StartModeItem startModeItem : startModes) {
                Flags flag = mLineStartProcedure.getStartModeFlag();
                if (startModeItem.getFlag().equals(flag)) {
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
    }

    @Override
    public void onPause() {
        super.onPause();

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    public void onClick(StartModeItem startMode) {
        boolean sameFlag = false;
        Flags flag = mLineStartProcedure.getStartModeFlag();
        mLineStartProcedure.setStartModeFlag(MillisecondsTimePoint.now(), startMode.getFlag());
        if (startMode.getFlag().equals(flag)) {
            sameFlag = true;
        }
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
