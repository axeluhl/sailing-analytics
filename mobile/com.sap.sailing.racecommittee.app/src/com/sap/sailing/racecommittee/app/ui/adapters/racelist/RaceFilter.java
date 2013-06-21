package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.widget.Filter;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.ui.fragments.lists.ManagedRaceListFragment.FilterMode;

public class RaceFilter extends Filter {

    public interface FilterSubscriber {
        void onResult(List<RaceListDataType> filtered);
    }

    private final FilterSubscriber subscriber;
    private Collection<RaceListDataType> items;
    private FilterMode filterMode;

    public RaceFilter(Collection<RaceListDataType> items, FilterSubscriber resultSubscriber) {
        this.items = items;
        this.subscriber = resultSubscriber;
    }

    public void filterByMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        filter("");
    }

    private static FilterResults createResults(Collection<RaceListDataType> result) {
        FilterResults results = new FilterResults();
        results.values = new ArrayList<RaceListDataType>(result);
        results.count = result.size();
        return results;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        if (filterMode == null || filterMode.equals(FilterMode.ALL)) {
            return createResults(items);
        }

        List<RaceListDataType> filteredItems = new ArrayList<RaceListDataType>();
        // boolean changedGroup = true;
        RaceListDataTypeRace newestFinishedItem = null;
        RaceListDataTypeRace nextUnscheduldedItem = null;

        // To filter the races...
        List<RaceListDataType> array = new ArrayList<RaceListDataType>(items);
        for (RaceListDataType item : array) {
            int itemIndex = array.indexOf(item);
            // ... we always take the headers...
            if (item instanceof RaceListDataTypeHeader) {
                RaceListDataTypeHeader headerItem = (RaceListDataTypeHeader) item;
                filteredItems.add(headerItem);

            } else if (item instanceof RaceListDataTypeRace) {
                // ... but...
                RaceListDataTypeRace raceItem = (RaceListDataTypeRace) item;
                RaceLogRaceStatus status = raceItem.getCurrentStatus();
                if (status.equals(RaceLogRaceStatus.FINISHED)) {
                    // ... only the newest (by sort order) finished race...
                    if (newestFinishedItem != null) {
                        filteredItems.remove(newestFinishedItem);
                    }
                    filteredItems.add(raceItem);
                    newestFinishedItem = raceItem;
                } else if (status.equals(RaceLogRaceStatus.UNSCHEDULED)) {
                    // ... the first (by sort order) unscheduled race and the next after the (current) newest
                    // finished or scheduled...
                    if (nextUnscheduldedItem == null) {
                        filteredItems.add(raceItem);
                        nextUnscheduldedItem = raceItem;
                    } else if (array.indexOf(newestFinishedItem) == itemIndex - 1) {
                        filteredItems.add(raceItem);
                    }
                } else if (RaceLogRaceStatus.isActive(status)) {
                    // ... and all active races...
                    filteredItems.add(raceItem);
                }
            } else {
                throw new IllegalStateException("Unknown race list item type.");
            }

            // ... and every time we are starting with a new header reset the state!
            boolean isGroupEnd = itemIndex == array.size() - 1
                    || array.get(itemIndex + 1) instanceof RaceListDataTypeHeader;
            if (isGroupEnd) {
                if (filteredItems.size() > 0) {
                    RaceListDataType lastItemOfPreviousGroup = filteredItems.get(filteredItems.size() - 1);
                    if (lastItemOfPreviousGroup != nextUnscheduldedItem) {
                        int indexOfLastItemOfPreviousGroup = array.indexOf(lastItemOfPreviousGroup);
                        // ... if there are some items filtered between this header and the last item
                        // of the previous group ...
                        if (indexOfLastItemOfPreviousGroup != (array.indexOf(item) - 1)
                                && indexOfLastItemOfPreviousGroup != array.size() - 1) {
                            RaceListDataType followingItem = array.get(indexOfLastItemOfPreviousGroup + 1);
                            if (followingItem instanceof RaceListDataTypeRace) {
                                RaceListDataTypeRace raceItem = (RaceListDataTypeRace) followingItem;
                                // ... take it if it's unscheduled!
                                if (raceItem.getCurrentStatus().equals(RaceLogRaceStatus.UNSCHEDULED)) {
                                    filteredItems.add(followingItem);
                                }
                            }
                        }
                    }
                }
                newestFinishedItem = null;
                nextUnscheduldedItem = null;
            }
        }

        return createResults(filteredItems);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        subscriber.onResult((List<RaceListDataType>) results.values);
    }

}
