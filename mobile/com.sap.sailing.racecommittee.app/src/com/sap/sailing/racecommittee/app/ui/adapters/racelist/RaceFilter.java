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
        boolean changedGroup = true;
        RaceListDataTypeRace newestFinishedItem = null;
        RaceListDataTypeRace nextUnscheduldedItem = null;

        // To filter the races...
        List<RaceListDataType> array = new ArrayList<RaceListDataType>(items);
        for (RaceListDataType item : array) {
            // ... we always take the headers...
            if (item instanceof RaceListDataTypeHeader) {
                changedGroup = true;
                RaceListDataTypeHeader headerItem = (RaceListDataTypeHeader) item;
                filteredItems.add(headerItem);

            } else {
                changedGroup = false;
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
                    // finished...
                    if (nextUnscheduldedItem == null) {
                        filteredItems.add(raceItem);
                        nextUnscheduldedItem = raceItem;
                    } else if (array.indexOf(newestFinishedItem) == array.indexOf(item) - 1) {
                        filteredItems.add(raceItem);
                    }
                } else if (RaceLogRaceStatus.isActive(status)) {
                    // ... and all active races...
                    filteredItems.add(raceItem);
                }
            }

            // ... and every time we are starting with a new header reset the state!
            if (changedGroup) {
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
