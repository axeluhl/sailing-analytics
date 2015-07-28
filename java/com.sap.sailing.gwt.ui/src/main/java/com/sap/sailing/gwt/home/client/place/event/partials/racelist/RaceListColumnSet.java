package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import static com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn.ColumnVisibility.ALWAYS;
import static com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn.ColumnVisibility.LARGE;
import static com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn.ColumnVisibility.MEDIUM;

import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.gwt.home.client.place.event.partials.racelist.SortableRaceListColumn.ColumnVisibility;

public class RaceListColumnSet {
    
    private final Set<SortableRaceListColumn<?, ?>> columns = new LinkedHashSet<>();
    private final int visibilityAlwaysQuota, visibilityMediumQuota;
    
    public RaceListColumnSet(int visibilityAlwaysQuota, int visibilityMediumQuota) {
        this.visibilityAlwaysQuota = visibilityAlwaysQuota;
        this.visibilityMediumQuota = visibilityMediumQuota;
    }
    
    public void addColumn(SortableRaceListColumn<?, ?> column) {
        columns.add(column);
    }
    
    public void updateColumnVisibilities() {
        int visibleColumnCount = 0;
        for (SortableRaceListColumn<?, ?> column : columns) {
            if (column.isShowDetails()) {
                column.setColumnVisibility(getColumnVisibilityByQuota(visibleColumnCount++));
            }
        }
    }
    
    private ColumnVisibility getColumnVisibilityByQuota(int i) {
        return (i < visibilityAlwaysQuota) ? ALWAYS : (i < visibilityAlwaysQuota + visibilityMediumQuota) ? MEDIUM : LARGE;
    }

}
