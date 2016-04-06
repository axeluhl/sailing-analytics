package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.LinkedHashMap;
import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.logging.LogEvent;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.adapters.AbortFlagsAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.AbortFlagsAdapter.AbortFlagItemClick;
import com.sap.sailing.racecommittee.app.ui.adapters.AbortNovemberAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceListFragment;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
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

        HeaderLayout header = ViewHelper.get(layout, R.id.header);
        if (header != null) {
            header.setHeaderText(getArguments().getString(HEADER_TEXT, getString(R.string.not_available)));
            header.setHeaderOnClickListener(new View.OnClickListener() {
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
                state.setAdvancePass(now);
                break;

            case NOVEMBER:
                logFlag(flag);
                abortRunningRaces(now, flag);
                break;

            default:
                logFlag(flag);
                state.setAdvancePass(now);
                break;
        }
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

    private void abortRunningRaces(final TimePoint now, final Flags flag) {
        final RacingActivity activity = (RacingActivity) getActivity();
        final LinkedHashMap<String, ManagedRace> races = activity.getRunningRaces();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_AlertDialog);
        View layout = LayoutInflater.from(builder.getContext()).inflate(R.layout.race_november_dialog, null);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.abort_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        final AbortNovemberAdapter adapter = new AbortNovemberAdapter(races, getRace());
        recyclerView.setAdapter(adapter);
        builder.setTitle(R.string.abort_title);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<ManagedRace> selectedRaces = adapter.getSelected();
                for (ManagedRace race : selectedRaces) {
                    List<ManagedRace> racesToAbort = activity
                        .getChildRaces(race, new RaceLogRaceStatus[] { RaceLogRaceStatus.STARTPHASE, RaceLogRaceStatus.RUNNING });
                    for (ManagedRace abort : racesToAbort) {
                        if (!isSelected(selectedRaces, abort)) {
                            RaceState raceState = abort.getState();
                            Duration startTimeDiff = raceState.getStartTimeFinderResult().getStartTimeDiff();
                            SimpleRaceLogIdentifier parent = Util.get(raceState.getStartTimeFinderResult().getRacesDependingOn(), 0);
                            abortRace(raceState);
                            raceState.forceNewDependentStartTime(now, startTimeDiff, parent);
                        }
                    }

                    List<ManagedRace> racesToLeave = activity
                        .getChildRaces(race, new RaceLogRaceStatus[] { RaceLogRaceStatus.FINISHING, RaceLogRaceStatus.FINISHED });
                    for (ManagedRace leave : racesToLeave) {
                        leave.getState().forceNewStartTime(now, leave.getState().getStartTimeFinderResult().getStartTime());
                    }
                    if (!getRace().equals(race)) {
                        abortRace(race.getState());
                    }
                }
                if (flag != Flags.NONE) {
                    setProtestTime();
                }
                abortRace(getRaceState());
            }

            private void abortRace(RaceState raceState) {
                raceState.setAborted(now, /* postponed */ false, flag);
                raceState.setAdvancePass(now);
            }
        });
        builder.setNegativeButton(R.string.abort_dismiss, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        int maxItemHeight = (int) getItemHeight(activity) * races.size() + getActivity().getResources().getDimensionPixelSize(R.dimen.abort_dialog_height);
        lp.height = Math.min(maxItemHeight, (int) getMaxScreenHeight(activity));
        dialog.getWindow().setAttributes(lp);
    }

    private boolean isSelected(List<ManagedRace> selectedRaces, ManagedRace race) {
        for (ManagedRace selected : selectedRaces) {
            if (selected.equals(race)) {
                return true;
            }
        }
        return false;
    }

    private float getItemHeight(Activity activity) {
        TypedValue value = new android.util.TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        TypedValue.coerceToString(value.type, value.data);
        DisplayMetrics metrics = new android.util.DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return value.getDimension(metrics);
    }

    private float getMaxScreenHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels * 0.85f;
    }
}
