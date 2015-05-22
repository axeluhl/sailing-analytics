package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.widget.Filter;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceListFragment.FilterMode;

/**
 * Filters races by status.
 * 
 * For each "run" of unscheduled races only the first will be taken.
 * For each "run" of finished races only the last will be taken.
 */
public class RaceFilter extends Filter {

    public interface FilterSubscriber {
        void onResult(List<RaceListDataType> filtered);
    }

    protected final FilterSubscriber subscriber;
    protected Collection<RaceListDataType> items;
    protected FilterMode filterMode;

    public RaceFilter(Collection<RaceListDataType> items, FilterSubscriber resultSubscriber) {
        this.items = items;
        this.subscriber = resultSubscriber;
    }

    public void filterByMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        filter("");
    }

    protected static FilterResults createResults(Collection<RaceListDataType> result) {
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
        RaceListDataTypeRace currentUnscheduledItem = null;
        RaceListDataTypeRace currentFinishedItem = null;
        
        List<RaceListDataType> allItems = new ArrayList<RaceListDataType>(items);
        for (RaceListDataType item : allItems) {
            if (item instanceof RaceListDataTypeHeader) {
                RaceListDataTypeHeader headerItem = (RaceListDataTypeHeader) item;
                filteredItems.add(headerItem);
                
                // new run for both types!
                currentUnscheduledItem = null;
                currentFinishedItem = null;
            } else if (item instanceof RaceListDataTypeRace) {
                RaceListDataTypeRace raceItem = (RaceListDataTypeRace) item;
                RaceLogRaceStatus status = raceItem.getCurrentStatus();
                if (currentUnscheduledItem == null && status.equals(RaceLogRaceStatus.UNSCHEDULED)) {
                    filteredItems.add(raceItem);
                    currentUnscheduledItem = raceItem;
                } else if (status.equals(RaceLogRaceStatus.FINISHED)) {
                    if (filteredItems.contains(currentFinishedItem)) {
                        filteredItems.remove(currentFinishedItem);
                    }
                    filteredItems.add(raceItem);
                    currentFinishedItem = raceItem;
                    currentUnscheduledItem = null;
                } else if (RaceLogRaceStatus.isActive(status)) {
                    filteredItems.add(raceItem);
                    // new run for both types!
                    currentUnscheduledItem = null;
                    currentFinishedItem = null;
                }
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
