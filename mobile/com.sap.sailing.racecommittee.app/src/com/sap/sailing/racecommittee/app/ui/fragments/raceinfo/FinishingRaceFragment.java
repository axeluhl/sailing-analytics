package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.adapters.finishing.CompetitorPositioningListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.finishing.CompetitorsAdapter;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

public class FinishingRaceFragment extends RaceFragment implements TickListener {
    
    private TextView countUpTextView;
    protected TextView nextFlagCountdown;
    private ImageButton novemberFlagButton;
    private ImageButton blueFlagButton;
    
    private DragSortListView positioningListView;
    private ListView competitorListView;
    
    private CompetitorsAdapter competitorsAdapter;
    private CompetitorPositioningListAdapter positioningAdapter;
    
    protected List<Competitor> competitors;
    protected List<Competitor> positionedCompetitors;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_finishing_view, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        countUpTextView = (TextView) getView().findViewById(R.id.raceCountUp);
        nextFlagCountdown = (TextView) getView().findViewById(R.id.nextFlagCountdown);
        
        blueFlagButton = (ImageButton) getView().findViewById(R.id.blueFlagButton);
        novemberFlagButton = (ImageButton) getView().findViewById(R.id.novemberButton);
        
        competitors = new ArrayList<Competitor>();
        Util.addAll(getRace().getCompetitors(), competitors);
        
        positionedCompetitors = new ArrayList<Competitor>();
        competitorsAdapter = new CompetitorsAdapter(getActivity(), R.layout.welter_grid_competitor_cell, competitors);
        
        positioningAdapter = new CompetitorPositioningListAdapter(getActivity(), R.layout.welter_positioning_item, positionedCompetitors);
        
        positioningListView = (DragSortListView) getView().findViewById(R.id.listViewPositioningList);
        positioningListView.setAdapter(positioningAdapter);
        positioningListView.setDropListener(new DragSortListView.DropListener() {

            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    Competitor item = positioningAdapter.getItem(from);
                    positioningAdapter.remove(item);
                    positioningAdapter.insert(item, to);
                }
            }
        });
        
        positioningListView.setRemoveListener(new DragSortListView.RemoveListener() {
            
            @Override
            public void remove(int toBeRemoved) {
                Competitor item = positioningAdapter.getItem(toBeRemoved);
                onCompetitorRemovedFromPositioningList(item);
            }
        });

        competitorListView = (ListView) getView().findViewById(R.id.gridViewCompetitors);
        competitorListView.setAdapter(competitorsAdapter);

        blueFlagButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showRemoveBlueFlagDialog();
            }
        });

        novemberFlagButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                showChooseAPNovemberDialog();
                ExLog.i(ExLog.FLAG_NOVEMBER, getRace().getId().toString(), getActivity());
            }
        });

        competitorListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                Competitor competitor = (Competitor) competitorsAdapter.getItem(position);
                ExLog.i("RaceFinishingFragment", "Grid is clicked at position " + position + " for competitor " + competitor.getName());
                onCompetitorClickedOnGrid(competitor);
            }
        });
        
    }
    
    @Override
    public void onStart() {
        super.onStart();
        TickSingleton.INSTANCE.registerListener(this);
        notifyTick();
    }

    @Override
    public void onStop() {
        super.onStop();
        TickSingleton.INSTANCE.unregisterListener(this);
    }

    protected void onCompetitorClickedOnGrid(Competitor competitor) {
        addNewCompetitorToPositioningList(competitor);
        removeCompetitorFromGrid(competitor);
    }

    protected void removeCompetitorFromGrid(Competitor competitor) {
        competitors.remove(competitor);
        competitorsAdapter.notifyDataSetChanged();
    }

    private void addNewCompetitorToPositioningList(Competitor competitor) {
        positionedCompetitors.add(competitor);
        positioningAdapter.notifyDataSetChanged();
    }
    
    protected void onCompetitorRemovedFromPositioningList(Competitor competitor) {
        addNewCompetitorToCompetitorList(competitor);
        removeCompetitorFromPositionings(competitor);
    }
    
    private void addNewCompetitorToCompetitorList(Competitor competitor) {
        competitors.add(competitor);
        competitorsAdapter.notifyDataSetChanged();
    }
    
    protected void removeCompetitorFromPositionings(Competitor competitor) {
        positionedCompetitors.remove(competitor);
        positioningAdapter.notifyDataSetChanged();
    }

    protected void setCountdownLabels(long millisecondsSinceStart) {
        setStarttimeCountupLabel(millisecondsSinceStart);
    }

    private void setStarttimeCountupLabel(long millisecondsSinceStart) {
        countUpTextView.setText(String.format(getActivity().getResources().getString(R.string.race_running_since_template),
                prettyTimeString(millisecondsSinceStart), getRace().getName()));
    }

    protected CharSequence prettyTimeString(long time) {
        int secondsStart = (int) (time / 1000);
        int hours = secondsStart / 3600;
        int minutes = (secondsStart % 3600) / 60;
        int seconds = (secondsStart % 60);
        String timePattern = "%s:%s:%s";
        String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
        String minutesString = minutes < 10 ? "0" + minutes : "" + minutes;
        String hoursString = hours < 10 ? "0" + hours : "" + hours;
        return String.format(timePattern, hoursString, minutesString,
                secondsString);
    }
    
    protected void setRaceFinishCountDownLabel() {
        Date raceFinishingTimeDate = getRace().getState().getFinishingStartTime().asDate();

        nextFlagCountdown.setText(String.format(getActivity().getResources().getString(R.string.finishing_limit_template), 
                getFormattedTime(raceFinishingTimeDate)));
    }

    private String getFormattedTime(Date time) {
        return getFormattedTimePart(time.getHours()) + ":" + getFormattedTimePart(time.getMinutes()) + ":" + getFormattedTimePart(time.getSeconds());
    }

    private String getFormattedTimePart(int timePart) {
        return (timePart < 10) ? "0" + timePart : String.valueOf(timePart);
    }

    private void showChooseAPNovemberDialog() {
//        FragmentManager fragmentManager = getFragmentManager();
//
//        RaceChooseAPNovemberDialog fragment = new RaceChooseAPNovemberDialog();
//
//        Bundle args = getParameterBundle();
//        fragment.setArguments(args);
//
//        fragment.show(fragmentManager, "dialogAPNovemberMode");
    }

    private void showRemoveBlueFlagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getResources().getString(R.string.confirmation_blue_flag_remove))
        .setCancelable(true)
        .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.FLAG_BLUE_REMOVE, getRace().getId().toString(), getActivity());
                getRace().getState().onRaceFinished(MillisecondsTimePoint.now());
            }
        })
        .setNegativeButton(getActivity().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ExLog.i(ExLog.FLAG_BLUE_REMOVE_NO, getRace().getId().toString(), getActivity());
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void notifyTick() {
        if (getRace().getState().getStartTime() == null)
            return;

        long millisSinceStart = System.currentTimeMillis() - getRace().getState().getStartTime().asMillis();
        setCountdownLabels(millisSinceStart);

        setRaceFinishCountDownLabel();
    }
}
