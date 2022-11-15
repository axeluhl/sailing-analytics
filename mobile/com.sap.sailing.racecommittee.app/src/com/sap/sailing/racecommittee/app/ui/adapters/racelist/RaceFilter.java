package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.racegroup.CurrentRaceFilter;
import com.sap.sailing.domain.base.racegroup.impl.CurrentRaceFilterImpl;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceListFragment.FilterMode;

import android.widget.Filter;

/**
 * Filters {@link ManagedRace} objects by status and place in the regatta structure. See {@link CurrentRaceFilter}. If
 * the {@link #filterMode} is {@link FilterMode#ALL}, no filtering takes place at all, and all races will be displayed.
 * If the {@link #filterMode} is {@link FilterMode#ACTIVE}, the set of races to be displayed is computed using a
 * {@link CurrentRaceFilter}.
 */
public class RaceFilter extends Filter {
    /**
     * A filter that is updated each time an update for {@link #allRaces} is announced by
     * {@link #refreshRegattaStructures()}.
     */
    private CurrentRaceFilter<ManagedRace> currentRaceFilter;

    public interface FilterSubscriber {
        void onResult(List<ManagedRace> filtered);
    }

    private final FilterSubscriber subscriber;
    private final Set<ManagedRace> allRaces;
    private FilterMode filterMode;

    /**
     * @param allRaces
     *            callers must call {@link #refreshRegattaStructures()} whenever the contents of this set have changed
     * @param resultSubscriber
     *            triggered when the filter results have changed
     */
    public RaceFilter(Set<ManagedRace> allRaces, FilterSubscriber resultSubscriber) {
        this.allRaces = allRaces;
        this.subscriber = resultSubscriber;
        refreshRegattaStructures();
    }

    public void filterByMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        filter("");
    }

    protected static FilterResults createResults(Set<ManagedRace> result) {
        FilterResults results = new FilterResults();
        results.values = new ArrayList<>(result);
        results.count = result.size();
        return results;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        final FilterResults result;
        if (filterMode == null || filterMode.equals(FilterMode.ALL)) {
            result = createResults(allRaces);
        } else {
            // filter using the CurrentRaceFilter
            result = createResults(currentRaceFilter.getCurrentRaces());
        }
        return result;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        @SuppressWarnings("unchecked")
        final List<ManagedRace> filterResults = (List<ManagedRace>) results.values;
        subscriber.onResult(filterResults);
    }

    public void refreshRegattaStructures() {
        this.currentRaceFilter = new CurrentRaceFilterImpl<ManagedRace>(allRaces);
        filter("");
    }

}
