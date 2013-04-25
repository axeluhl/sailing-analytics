package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.impl.DomainFactoryImpl;
import com.sap.sailing.racecommittee.app.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.adapters.finishing.CompetitorPositioningListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.finishing.CompetitorsAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.NamedComparator;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;

public class PositioningFragment extends RaceDialogFragment {

    private DragSortListView positioningListView;
    private ListView competitorListView;
    private DragSortController dragSortController;
    
    private CompetitorsAdapter competitorsAdapter;
    private CompetitorPositioningListAdapter positioningAdapter;
    
    protected List<Competitor> competitors;
    protected List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors;
    protected Comparator<Named> competitorComparator;
    
    private int dragStartMode = DragSortController.ON_DRAG;
    private boolean removeEnabled = true;
    private int removeMode = DragSortController.FLING_REMOVE;
    private boolean sortEnabled = true;
    private boolean dragEnabled = true;
    
    private boolean isDialog() {
        return getDialog() != null;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (isDialog()) {
            getDialog().setTitle("Set positionings");
        }
        return inflater.inflate(R.layout.race_positioning_view, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (isDialog()) {
            Button okButton = (Button) getView().findViewById(R.id.buttonPositionOk);
            okButton.setVisibility(Button.VISIBLE);
            okButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getRace().getState().setFinishPositioningConfirmed();
                    dismiss();
                }
            });
        }
        
        competitors = new ArrayList<Competitor>();
        Util.addAll(getRace().getCompetitors(), competitors);
        competitorComparator = new NamedComparator();
        Collections.sort(competitors, competitorComparator);
        competitorsAdapter = new CompetitorsAdapter(getActivity(), R.layout.welter_grid_competitor_cell, competitors);
        
        
        positionedCompetitors = initializeFinishPositioningList();
        deletePositionedCompetitorsFromUnpositionedList();
        positioningAdapter = new CompetitorPositioningListAdapter(getActivity(), R.layout.welter_positioning_item, positionedCompetitors);
        
        positioningListView = (DragSortListView) getView().findViewById(R.id.dragSortListPositioning);
        positioningListView.setAdapter(positioningAdapter);
        positioningListView.setDropListener(new DragSortListView.DropListener() {

            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    Triple<Serializable, String, MaxPointsReason> item = positioningAdapter.getItem(from);
                    setItemToNewPositioning(from, to, item);
                    getRace().getState().setFinishPositioningListChanged(positionedCompetitors);
                }
            }
        });
        
        positioningListView.setRemoveListener(new DragSortListView.RemoveListener() {
            
            @Override
            public void remove(int toBeRemoved) {
                Triple<Serializable, String, MaxPointsReason> item = positioningAdapter.getItem(toBeRemoved);
                onCompetitorRemovedFromPositioningList(item);
            }
        });

        positioningListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                createMaxPointsReasonSelectionDialog(positionedCompetitors.get(position));
                return true;
            } 
        });
        
        dragSortController = buildDragSortController(positioningListView);
        positioningListView.setFloatViewManager(dragSortController);
        positioningListView.setOnTouchListener(dragSortController);
        positioningListView.setDragEnabled(dragEnabled);

        competitorListView = (ListView) getView().findViewById(R.id.gridViewPositioning);
        competitorListView.setAdapter(competitorsAdapter);

        competitorListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                Competitor competitor = (Competitor) competitorsAdapter.getItem(position);
                ExLog.i("RaceFinishingFragment", "Grid is clicked at position " + position + " for competitor " + competitor.getName());
                onCompetitorClickedOnGrid(competitor);
            }
        });
    }
    
    /**
     * Creates a DragSortController and thereby defines the behaviour of the drag sort list
     */
    public DragSortController buildDragSortController(DragSortListView dragSortListView) {
        DragSortController controller = new DragSortController(dragSortListView);
        controller.setDragHandleId(R.id.drag_handle);
        controller.setFlingHandleId(R.id.drag_handle);
        controller.setRemoveEnabled(removeEnabled);
        controller.setSortEnabled(sortEnabled);
        controller.setDragInitMode(dragStartMode);
        controller.setRemoveMode(removeMode);
        controller.setBackgroundColor(getActivity().getResources().getColor(R.color.welter_medium_blue));
        return controller;
    }
    
    private void deletePositionedCompetitorsFromUnpositionedList() {
        for (Triple<Serializable, String, MaxPointsReason> positionedItem : positionedCompetitors) {
            Competitor competitor = DomainFactoryImpl.INSTANCE.getExistingCompetitorById(positionedItem.getA());
            competitors.remove(competitor);
        }
    }
    
    private List<Triple<Serializable, String, MaxPointsReason>> initializeFinishPositioningList() {
        List<Triple<Serializable, String, MaxPointsReason>> positionings;
        if (getRace().getState().getFinishPositioningList() != null) {
            positionings = getRace().getState().getFinishPositioningList();
        } else {
            positionings = new ArrayList<Triple<Serializable, String, MaxPointsReason>>();
        }
        return positionings;
    }
    
    private void setItemToNewPositioning(int from, int to, Triple<Serializable, String, MaxPointsReason> item) {
        positioningAdapter.remove(item);
        positioningAdapter.insert(item, to);
    }

    private void createMaxPointsReasonSelectionDialog(final Triple<Serializable, String, MaxPointsReason> item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final CharSequence[] maxPointsReasons = getAllMaxPointsReasons();
        builder.setTitle(R.string.select_penalty_reason)
        .setItems(maxPointsReasons, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                setMaxPointsReasonForItem(item, maxPointsReasons[position]);
            }
        });
        builder.create();
        builder.show();
    }
    
    private CharSequence[] getAllMaxPointsReasons() {
        List<CharSequence> result = new ArrayList<CharSequence>();
        for (MaxPointsReason reason : MaxPointsReason.values()) {
            result.add(reason.name());
        }
        return result.toArray(new CharSequence[result.size()]);
    }

    protected void setMaxPointsReasonForItem(Triple<Serializable, String, MaxPointsReason> item, CharSequence maxPointsReasonName) {
        MaxPointsReason maxPointsReason = MaxPointsReason.valueOf(maxPointsReasonName.toString());
        Triple<Serializable, String, MaxPointsReason> newItem = new Triple<Serializable, String, MaxPointsReason>(item.getA(), item.getB(), maxPointsReason);
        int currentIndexOfItem = positionedCompetitors.indexOf(item);
        replaceItemInPositioningList(currentIndexOfItem, item, newItem);
        
        if (!maxPointsReason.equals(MaxPointsReason.NONE)) {
            setCompetitorToBottomOfPositioningList(newItem);
        }
        getRace().getState().setFinishPositioningListChanged(positionedCompetitors);
    }

    private void replaceItemInPositioningList(int currentIndexOfItem, Triple<Serializable, String, MaxPointsReason> item, Triple<Serializable, String, MaxPointsReason> newItem) {
        positioningAdapter.remove(item);
        positioningAdapter.insert(newItem, currentIndexOfItem);
    }

    private void setCompetitorToBottomOfPositioningList(Triple<Serializable, String, MaxPointsReason> item) {
        int currentIndex = positionedCompetitors.indexOf(item);
        int lastIndex = positionedCompetitors.size() - 1;
        setItemToNewPositioning(currentIndex, lastIndex, item);
    }

    protected void onCompetitorClickedOnGrid(Competitor competitor) {
        addNewCompetitorToPositioningList(competitor);
        removeCompetitorFromGrid(competitor);
        getRace().getState().setFinishPositioningListChanged(positionedCompetitors);
    }

    protected void removeCompetitorFromGrid(Competitor competitor) {
        competitors.remove(competitor);
        Collections.sort(competitors, competitorComparator);
        competitorsAdapter.notifyDataSetChanged();
    }

    private void addNewCompetitorToPositioningList(Competitor competitor) {
        
        positionedCompetitors.add(new Triple<Serializable, String, MaxPointsReason>(competitor.getId(), competitor.getName(), MaxPointsReason.NONE));
        positioningAdapter.notifyDataSetChanged();
    }
    
    protected void onCompetitorRemovedFromPositioningList(Triple<Serializable, String, MaxPointsReason> item) {
        Competitor competitor = DomainFactoryImpl.INSTANCE.getExistingCompetitorById(item.getA());
        addNewCompetitorToCompetitorList(competitor);
        removeCompetitorFromPositionings(item);
        getRace().getState().setFinishPositioningListChanged(positionedCompetitors);
    }
    
    private void addNewCompetitorToCompetitorList(Competitor competitor) {
        competitors.add(competitor);
        Collections.sort(competitors, competitorComparator);
        competitorsAdapter.notifyDataSetChanged();
    }
    
    protected void removeCompetitorFromPositionings(Triple<Serializable, String, MaxPointsReason> item) {
        positionedCompetitors.remove(item);
        positioningAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyTick() {
        
    }

}
