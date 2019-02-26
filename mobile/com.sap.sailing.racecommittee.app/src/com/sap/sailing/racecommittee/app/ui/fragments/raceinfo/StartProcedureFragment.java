package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;

import com.sap.sailing.android.shared.util.AppUtils;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.CheckedItemAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.checked.StartProcedureItem;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class StartProcedureFragment extends BaseFragment {

    private final ArrayList<StartProcedureItem> startProcedure = new ArrayList<>();
    private HeaderLayout mHeader;

    public StartProcedureFragment() {

    }

    public static StartProcedureFragment newInstance(@START_MODE_VALUES int startMode) {
        StartProcedureFragment fragment = new StartProcedureFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_schedule_procedure, container, false);

        mHeader = ViewHelper.get(layout, R.id.header);
        if (mHeader != null) {
            mHeader.setHeaderOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    goHome();
                }
            });
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null && getArguments() != null) {
            switch (getArguments().getInt(START_MODE, START_MODE_PRESETUP)) {
            case START_MODE_PLANNED:
                if (AppUtils.with(getActivity()).isLandscape()) {
                    mHeader.setVisibility(View.GONE);
                }
                break;

            default:
                break;
            }
        }
        RacingProcedure racingProcedure = getRaceState().getRacingProcedure();
        int position = 0;
        int selected = -1;
        for (RacingProcedureType procedureType : RacingProcedureType.validValues()) {
            StartProcedureItem item = new StartProcedureItem(procedureType);
            startProcedure.add(item);
            if (racingProcedure != null && racingProcedure.getType().equals(procedureType)) {
                selected = position;
            }
            position++;
        }

        ListView listView = (ListView) getActivity().findViewById(R.id.listView);
        if (listView != null) {
            final CheckedItemAdapter adapter = new CheckedItemAdapter(getActivity(), startProcedure);
            adapter.setCheckedPosition(selected);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.setCheckedPosition(position);
                    StartProcedureItem item = (StartProcedureItem) adapter.getItem(position);
                    if (item != null) {
                        onClick(item.getProcedureType());
                    }
                }
            });
        }
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

    public void onClick(RacingProcedureType procedureType) {
        boolean sameProcedure = false;
        if (getRaceState().getRacingProcedure().getType() == procedureType) {
            sameProcedure = true;
        } else {
            getRaceState().setRacingProcedure(MillisecondsTimePoint.now(), procedureType);
        }
        if (getArguments() != null && getArguments().getInt(START_MODE, START_MODE_PRESETUP) == START_MODE_PRESETUP) {
            openMainScheduleFragment();
        } else {
            if (sameProcedure) {
                sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
            } else {
                getRaceState().forceNewStartTime(MillisecondsTimePoint.now(), getRaceState().getStartTime());
            }
        }
    }
}
