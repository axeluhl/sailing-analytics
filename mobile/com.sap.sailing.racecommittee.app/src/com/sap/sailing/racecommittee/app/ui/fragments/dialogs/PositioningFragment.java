package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Loader;
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
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.data.ReadonlyDataManager;
import com.sap.sailing.racecommittee.app.data.clients.LoadClient;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.ui.adapters.finishing.CompetitorPositioningListAdapter;
import com.sap.sailing.racecommittee.app.ui.adapters.finishing.CompetitorsAdapter;
import com.sap.sailing.racecommittee.app.ui.comparators.NaturalNamedComparator;
import com.sap.sse.common.Named;
import com.sap.sse.common.Util;

public class PositioningFragment extends RaceDialogFragment {
    
    private DragSortListView positioningListView;
    private ListView competitorListView;
    private DragSortController dragSortController;

    private CompetitorsAdapter competitorsAdapter;
    private CompetitorPositioningListAdapter positioningAdapter;

    protected List<Competitor> competitors;
    protected CompetitorResults positionedCompetitors;
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
            getDialog().setTitle(R.string.set_positionings);
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
                    getRaceState().setFinishPositioningConfirmed(MillisecondsTimePoint.now());
                    dismiss();
                }
            });
        }

        competitors = new ArrayList<Competitor>();
        Util.addAll(getRace().getCompetitors(), competitors);
        competitorComparator = new NaturalNamedComparator();
        Collections.sort(competitors, competitorComparator);
        competitorsAdapter = new CompetitorsAdapter(getActivity(), R.layout.welter_grid_competitor_cell, competitors);

        loadCompetitors();

        positionedCompetitors = initializeFinishPositioningList();
        deletePositionedCompetitorsFromUnpositionedList();
        positioningAdapter = new CompetitorPositioningListAdapter(getActivity(), R.layout.welter_positioning_item,
                positionedCompetitors);

        positioningListView = (DragSortListView) getView().findViewById(R.id.dragSortListPositioning);
        positioningListView.setAdapter(positioningAdapter);
        positioningListView.setDropListener(new DragSortListView.DropListener() {

            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> item = positioningAdapter.getItem(from);
                    setItemToNewPositioning(from, to, item);
                    getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), positionedCompetitors);
                }
            }
        });

        positioningListView.setRemoveListener(new DragSortListView.RemoveListener() {

            @Override
            public void remove(int toBeRemoved) {
                com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> item = positioningAdapter.getItem(toBeRemoved);
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
                ExLog.i(getActivity(), "RaceFinishingFragment", "Grid is clicked at position " + position + " for competitor "
                        + competitor.getName());
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

    private void loadCompetitors() {
        getActivity().setProgressBarIndeterminateVisibility(true);
        
        // invalidate all competitors of this race
        ReadonlyDataManager dataManager = OnlineDataManager.create(getActivity());
        SharedDomainFactory domainFactory = dataManager.getDataStore().getDomainFactory();
        for (Competitor competitor : getRace().getCompetitors()) {
            domainFactory.getCompetitorStore().allowCompetitorResetToDefaults(competitor);
        }
        
        Loader<?> competitorLoaders = getLoaderManager().initLoader(0, null,
                dataManager.createCompetitorsLoader(getRace(), new LoadClient<Collection<Competitor>>() {

                    @Override
                    public void onLoadFailed(Exception reason) {
                        getActivity().setProgressBarIndeterminateVisibility(false);
                        Toast.makeText(getActivity(), String.format("Competitors: %s", reason.toString()),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onLoadSucceded(Collection<Competitor> data, boolean isCached) {
                        if (!isCached) {
                            getActivity().setProgressBarIndeterminateVisibility(false);
                        }
                        onLoadCompetitorsSucceeded(data);
                    }

                }));
        // Force load to get non-cached remote competitors...
        competitorLoaders.forceLoad();
    }

    protected void onLoadCompetitorsSucceeded(Collection<Competitor> data) {
        competitors.clear();
        competitors.addAll(data);
        Collections.sort(competitors, competitorComparator);
        deleteObsoleteCompetitorsFromPositionedList(data);
        deletePositionedCompetitorsFromUnpositionedList();
        competitorsAdapter.notifyDataSetChanged();
    }

    private void deleteObsoleteCompetitorsFromPositionedList(Collection<Competitor> validCompetitors) {
        List<com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason>> toBeDeleted = new ArrayList<com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason>>();
        for (com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> positionedItem : positionedCompetitors) {
            if (!validCompetitors.contains(getCompetitorStore().getExistingCompetitorById(positionedItem.getA()))) {
                toBeDeleted.add(positionedItem);
            }
        }
        positionedCompetitors.removeAll(toBeDeleted);
    }

    private void deletePositionedCompetitorsFromUnpositionedList() {
        for (com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> positionedItem : positionedCompetitors) {
            Competitor competitor = getCompetitorStore().getExistingCompetitorById(positionedItem.getA());
            competitors.remove(competitor);
        }
    }

    private CompetitorResults initializeFinishPositioningList() {
        CompetitorResults positionings = getRaceState().getFinishPositioningList();
        return positionings == null ? new CompetitorResultsImpl() : positionings;
    }

    private void setItemToNewPositioning(int from, int to, com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> item) {
        positioningAdapter.remove(item);
        positioningAdapter.insert(item, to);
    }

    private void createMaxPointsReasonSelectionDialog(final com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final CharSequence[] maxPointsReasons = getAllMaxPointsReasons();
        builder.setTitle(R.string.select_penalty_reason).setItems(maxPointsReasons,
                new DialogInterface.OnClickListener() {
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

    protected void setMaxPointsReasonForItem(com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> item,
            CharSequence maxPointsReasonName) {
        MaxPointsReason maxPointsReason = MaxPointsReason.valueOf(maxPointsReasonName.toString());
        com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> newItem = new com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason>(
                item.getA(), item.getB(), maxPointsReason);
        int currentIndexOfItem = positionedCompetitors.indexOf(item);
        replaceItemInPositioningList(currentIndexOfItem, item, newItem);

        if (!maxPointsReason.equals(MaxPointsReason.NONE)) {
            setCompetitorToBottomOfPositioningList(newItem);
        }
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), positionedCompetitors);
    }

    private void replaceItemInPositioningList(int currentIndexOfItem,
            com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> item, com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> newItem) {
        positioningAdapter.remove(item);
        positioningAdapter.insert(newItem, currentIndexOfItem);
    }

    private void setCompetitorToBottomOfPositioningList(com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> item) {
        int currentIndex = positionedCompetitors.indexOf(item);
        int lastIndex = positionedCompetitors.size() - 1;
        setItemToNewPositioning(currentIndex, lastIndex, item);
    }

    protected void onCompetitorClickedOnGrid(Competitor competitor) {
        addNewCompetitorToPositioningList(competitor);
        removeCompetitorFromGrid(competitor);
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), positionedCompetitors);
    }

    protected void removeCompetitorFromGrid(Competitor competitor) {
        competitors.remove(competitor);
        Collections.sort(competitors, competitorComparator);
        competitorsAdapter.notifyDataSetChanged();
    }

    private void addNewCompetitorToPositioningList(Competitor competitor) {

        positionedCompetitors.add(new com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason>(competitor.getId(), competitor
                .getName(), MaxPointsReason.NONE));
        positioningAdapter.notifyDataSetChanged();
    }

    protected void onCompetitorRemovedFromPositioningList(com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> item) {
        Competitor competitor = getCompetitorStore().getExistingCompetitorById(item.getA());
        addNewCompetitorToCompetitorList(competitor);
        removeCompetitorFromPositionings(item);
        getRaceState().setFinishPositioningListChanged(MillisecondsTimePoint.now(), positionedCompetitors);
    }

    private void addNewCompetitorToCompetitorList(Competitor competitor) {
        competitors.add(competitor);
        Collections.sort(competitors, competitorComparator);
        competitorsAdapter.notifyDataSetChanged();
    }

    protected void removeCompetitorFromPositionings(com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason> item) {
        positionedCompetitors.remove(item);
        positioningAdapter.notifyDataSetChanged();
    }

    private CompetitorStore getCompetitorStore() {
        return DataManager.create(getActivity()).getDataStore().getDomainFactory().getCompetitorStore();
    }

}
