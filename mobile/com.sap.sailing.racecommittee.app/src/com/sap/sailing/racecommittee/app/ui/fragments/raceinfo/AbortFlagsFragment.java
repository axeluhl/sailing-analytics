package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
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
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceListFragment;
import com.sap.sailing.racecommittee.app.ui.layouts.HeaderLayout;
import com.sap.sailing.racecommittee.app.utils.RaceHelper;
import com.sap.sse.common.Duration;
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

        HeaderLayout header = ViewHelper.get(layout, R.id.header);
        if (header != null) {
            header.setHeaderText(getArguments().getString(HEADER_TEXT, getString(R.string.not_available)));
            header.setOnClickListener(new View.OnClickListener() {
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
                abortChildRaces(now, state, flag);
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

    private void abortChildRaces(final TimePoint now, final RaceState state, final Flags flag) {
        RacingActivity activity = (RacingActivity) getActivity();
        final List<ManagedRace> races = activity
            .getChildRaces(getRace(), new RaceLogRaceStatus[] { RaceLogRaceStatus.STARTPHASE, RaceLogRaceStatus.RUNNING });

        if (races.size() > 0) {
            final ArrayList<String> raceList = new ArrayList<>();
            for (ManagedRace race : races) {
                raceList.add(RaceHelper.getShortReverseRaceName(race, " / ", getRace()));
            }
            final ArrayList<ManagedRace> raceChecked = new ArrayList<>();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
            builder.setTitle(getArguments().getString(HEADER_TEXT, getString(R.string.not_available)) + " " + getString(R.string.flags_dependent_races));
            builder.setMultiChoiceItems(raceList.toArray(new String[raceList.size()]), null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    ManagedRace race = races.get(which);
                    if (raceChecked.contains(race)) {
                        raceChecked.remove(race);
                    }
                    if (isChecked) {
                        raceChecked.add(race);
                    }
                }
            });
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (ManagedRace race : races) {
                        RaceState raceState = race.getState();
                        if (raceChecked.contains(race)) {
                            Duration startTimeDiff = raceState.getStartTimeFinderResult().getStartTimeDiff();
                            raceState.setAborted(now, /* postponed */ false, flag);
                            raceState.setAdvancePass(now);
                            raceState.forceNewDependentStartTime(now, startTimeDiff, RaceHelper.getSimpleRaceLogIdentifier(getRace()));
                        } else {
                            raceState.forceNewStartTime(now, raceState.getStartTimeFinderResult().getStartTime());
                        }
                    }
                    abortRace(now, state, flag);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    abortRace(now, state, flag);
                }
            });
            builder.setCancelable(false);
            builder.show();
        } else {
            abortRace(now, state, flag);
        }
    }

    private void abortRace(TimePoint now, RaceState state, Flags flag) {
        fixStartTime(now);
        state.setAborted(now, /* postponed */ false, flag);
        if (flag != Flags.NONE) {
            setProtestTime();
        }
        state.setAdvancePass(now);
    }

    private void fixStartTime(TimePoint now) {
        RacingActivity activity = (RacingActivity) getActivity();
        List<ManagedRace> races = activity
            .getChildRaces(getRace(), new RaceLogRaceStatus[] { RaceLogRaceStatus.PRESCHEDULED, RaceLogRaceStatus.SCHEDULED,
                RaceLogRaceStatus.FINISHING, RaceLogRaceStatus.FINISHED });

        for (ManagedRace race : races) {
            race.getState().forceNewStartTime(now, race.getState().getStartTimeFinderResult().getStartTime());
        }
    }
}
