package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TimePicker;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.RaceGroupSeriesFleet;
import com.sap.sailing.racecommittee.app.utils.ScreenHelper;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ProtestTimeDialogFragment extends AttachedDialogFragment {

    private static String ARGS_RACE_IDS = ProtestTimeDialogFragment.class.getSimpleName() + ".raceids";

    public static ProtestTimeDialogFragment newInstance(List<ManagedRace> races) {
        ArrayList<Serializable> raceIds = new ArrayList<>();
        for (ManagedRace race : races) {
            raceIds.add(race.getId());
        }
        Bundle args = new Bundle();
        args.putSerializable(ARGS_RACE_IDS, raceIds);

        ProtestTimeDialogFragment fragment = new ProtestTimeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private List<ManagedRace> races;
    private ListView racesList;
    private TimePicker timePicker;
    private View customView;

    public ProtestTimeDialogFragment() {
        races = new ArrayList<>();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        forceWrapContent(customView);
    }

    @Override
    protected AlertDialog.Builder createDialog(AlertDialog.Builder builder) {
        getRacesFromArguments();
        customView = setupView();
        return builder.setTitle(getString(R.string.protest_dialog_title)).setView(customView);
    }

    @Override
    protected CharSequence getNegativeButtonLabel() {
        return getString(R.string.cancel);
    }

    @Override
    protected CharSequence getPositiveButtonLabel() {
        return getString(R.string.choose);
    }

    @Override
    protected DialogListenerHost getHost() {
        return new DialogListenerHost() {
            @Override
            public DialogResultListener getListener() {
                return new DialogResultListener() {
                    @Override
                    public void onDialogNegativeButton(AttachedDialogFragment dialog) {
                        // no operation
                    }

                    @Override
                    public void onDialogPositiveButton(AttachedDialogFragment dialog) {
                        setAndAnnounceProtestTime();
                    }
                };
            }
        };
    }

    private View setupView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.protest_time_view, null);
        racesList = (ListView) view.findViewById(R.id.protest_time_races_list);
        setupRacesList(racesList);
        timePicker = (TimePicker) view.findViewById(R.id.protest_time_time_time_picker);
        setupTimePicker(timePicker);

        ViewGroup.LayoutParams layoutParams = racesList.getLayoutParams();
        layoutParams.height = 49 * races.size();
        int screenHeight = (int)(ScreenHelper.on(getActivity()).getScreenHeight() * 0.65);
        if (layoutParams.height > screenHeight) {
            layoutParams.height = screenHeight;
        }
        view.setLayoutParams(layoutParams);

        return view;
    }
    
    protected void forceWrapContent(View v) {
        // Start with the provided view
        View current = v;

        // Travel up the tree until fail, modifying the LayoutParams
        do {
            // Get the parent
            ViewParent parent = current.getParent();    

            // Check if the parent exists
            if (parent != null) {
                // Get the view
                try {
                    current = (View) parent;
                } catch (ClassCastException e) {
                    // This will happen when at the top view, it cannot be cast to a View
                    break;
                }

                // Modify the layout
                current.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
            }
        } while (current.getParent() != null);

        // Request a layout to be re-done
        current.requestLayout();
    }

    private void setupRacesList(ListView racesList) {
        racesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        racesList.setAdapter(new ProtestTimeAdapter(getActivity(), races));
        {
            int i = 0;
            for (ManagedRace race : races) {
                racesList.setItemChecked(i++, isFinishedToday(race));
            }
        }
        {
            SparseBooleanArray checked = racesList.getCheckedItemPositions();
            for (int i = 0; i < racesList.getCount(); i++) {
                if (checked.get(i)) {
                    racesList.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupTimePicker(TimePicker timePicker) {
        timePicker.setIs24HourView(true);

        TimePoint recentFinishedTime = null;
        for (ManagedRace race : races) {
            TimePoint currentFinishedTime = race.getState().getFinishedTime();
            if (currentFinishedTime != null
                    && (recentFinishedTime == null || recentFinishedTime.before(currentFinishedTime))) {
                recentFinishedTime = currentFinishedTime;
            }
        }
        Date suggestedDate = null;
        if (recentFinishedTime != null) {
            suggestedDate = recentFinishedTime.asDate();
        } else {
            suggestedDate = new Date();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(suggestedDate);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        timePicker.setCurrentHour(hours);
        timePicker.setCurrentMinute(minutes);
    }

    private void getRacesFromArguments() {
        Bundle args = getArguments();
        if (args == null) {
            throw new IllegalStateException("Arguments needed!");
        }

        ReadonlyDataManager manager = DataManager.create(getActivity());
        @SuppressWarnings("unchecked")
        ArrayList<Serializable> raceIds = (ArrayList<Serializable>) args.getSerializable(ARGS_RACE_IDS);
        for (Serializable id : raceIds) {
            races.add(manager.getDataStore().getRace(id));
        }
    }

    private void setAndAnnounceProtestTime() {
        List<ManagedRace> selectedRaces = getSelectedRaces();
        TimePoint protestTime = getProtestTime();
        TimePoint now = MillisecondsTimePoint.now();
        for (ManagedRace race : selectedRaces) {
            race.getState().setProtestTime(now, protestTime);
        }
    }

    private List<ManagedRace> getSelectedRaces() {
        List<ManagedRace> result = new ArrayList<ManagedRace>();
        SparseBooleanArray checked = racesList.getCheckedItemPositions();
        for (int i = 0; i < racesList.getCount(); i++) {
            if (checked.get(i)) {
                result.add(races.get(i));
            }
        }
        return result;
    }

    private TimePoint getProtestTime() {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        time.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        return new MillisecondsTimePoint(time.getTime());
    }

    private static boolean isFinishedToday(ManagedRace race) {
        if (race.getStatus().equals(RaceLogRaceStatus.FINISHED)) {
            FinishedTimeFinder analyzer = new FinishedTimeFinder(race.getRaceLog());
            TimePoint finishedTime = analyzer.analyze();
            if (finishedTime != null) {
                Calendar finishedCalendar = Calendar.getInstance();
                finishedCalendar.setTime(finishedTime.asDate());
                Calendar now = Calendar.getInstance();
                return finishedCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                        && finishedCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
            }
        }
        return false;
    }

    private static class ProtestTimeAdapter extends ArrayAdapter<ManagedRaceItem> {

        private static List<ManagedRaceItem> wrap(List<ManagedRace> races) {
            List<ManagedRaceItem> wrapped = new ArrayList<ManagedRaceItem>();
            for (ManagedRace race : races) {
                wrapped.add(new ManagedRaceItem(race));
            }
            return wrapped;
        }

        public ProtestTimeAdapter(Context context, List<ManagedRace> objects) {
            super(context, R.layout.themeable_protest_list_item, wrap(objects));
        }

    }

    private static class ManagedRaceItem {

        private RaceGroupSeriesFleet group;
        private ManagedRace race;

        public ManagedRaceItem(ManagedRace race) {
            this.race = race;
            this.group = new RaceGroupSeriesFleet(race);
        }

        @Override
        public String toString() {
            return String.format("%s - %s", group.getDisplayName(true), race.getRaceName());
        }

    }

}